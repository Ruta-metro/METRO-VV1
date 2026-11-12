package com.metroruta.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.metroruta.dto.Ruta;
import com.metroruta.model.CriterioRuta;
import com.metroruta.model.Estacion;
import com.metroruta.repository.EstacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

// pruebas con la red real del Metro; los resultados esperados se calcularon a mano
@SpringBootTest
class RutaServiceTest {

    @Autowired
    private RutaService rutaService;

    @Autowired
    private RedService redService;

    @Autowired
    private EstacionRepository estacionRepository;

    @BeforeEach
    void armarGrafo() {
        redService.reconstruir();
    }

    private Ruta rapida(String origen, String destino) {
        return rutaService.calcular(origen, destino, CriterioRuta.RAPIDA).orElseThrow();
    }

    private void verificar(String origen, String destino, int minutos, int transbordos) {
        Ruta ruta = rapida(origen, destino);
        assertEquals(minutos, ruta.minutos(), origen + " a " + destino);
        assertEquals(transbordos, ruta.transbordos(), origen + " a " + destino);
        // el total debe ser la suma de los tramos que se muestran
        assertEquals(ruta.minutos(), ruta.pasos().get(ruta.pasos().size() - 1).minutosAcumulados());
    }

    @Test
    void recorreTodaLaLineaA() {
        verificar("Niquía", "La Estrella", 40, 0); // 20 tramos de 2 min
    }

    @Test
    void recorreUnTramoDeLaLineaA() {
        verificar("Prado", "Poblado", 12, 0); // 6 tramos
    }

    @Test
    void cambiaDeLineaBALineaAEnSanAntonio() {
        verificar("Cisneros", "Alpujarra", 8, 1); // 2 + transbordo 4 + 2
    }

    @Test
    void cambiaDeLineaBAlTranvia() {
        verificar("San Javier", "Oriente", 32, 1); // 12 + 4 + 16
    }

    @Test
    void cambiaDelMetrocableKALineaAEnAcevedo() {
        verificar("Popular", "Niquía", 16, 1); // 6 + 4 + 6
    }

    @Test
    void conDosTransbordosDeUnaPuntaALaOtraDeLaRed() {
        verificar("Santo Domingo Savio", "San Javier", 44, 2); // 10 + 4 + 14 + 4 + 12
    }

    @Test
    void cambiaDelTranviaALineaB() {
        verificar("Loyola", "Floresta", 24, 1); // 12 + 4 + 8
    }

    @Test
    void ignoraMayusculasAlBuscarLasEstaciones() {
        assertEquals(12, rapida("prado", "POBLADO").minutos());
    }

    @Test
    void origenIgualADestinoEsError() {
        assertThrows(IllegalArgumentException.class, () -> rapida("Prado", "Prado"));
    }

    @Test
    void unaEstacionQueNoExisteEsError() {
        assertThrows(IllegalArgumentException.class, () -> rapida("Prado", "Atlantis"));
        assertThrows(IllegalArgumentException.class, () -> rapida("", "Prado"));
        assertThrows(IllegalArgumentException.class, () -> rapida(null, "Prado"));
    }

    @Test
    void sinCriterioEsError() {
        assertThrows(IllegalArgumentException.class, () -> rutaService.calcular("Prado", "Poblado", null));
    }

    @Test
    void cerrarPradoDesviaLaRutaPorMetroplus() {
        Estacion prado = estacionRepository.findByNombreIgnoreCase("Prado").orElseThrow();
        try {
            prado.setCerrada(true);
            estacionRepository.save(prado);
            redService.reconstruir();
            var desvio = rapida("Hospital", "Parque Berrío");
            assertTrue(desvio.pasos().stream().noneMatch(paso -> paso.estacion().equals("Prado")));
            assertTrue(desvio.pasos().stream().anyMatch(paso -> paso.linea().equals("Metroplús Línea 1")));
            assertTrue(rutaService.calcular("Prado", "Hospital", CriterioRuta.RAPIDA).isEmpty());
        } finally {
            prado.setCerrada(false);
            estacionRepository.save(prado);
            redService.reconstruir();
        }
        assertEquals(4, rapida("Hospital", "Parque Berrío").minutos());
    }
    @Test
    void florestaACaribePermiteElegirEntreTiempoYTransbordos() {
        var rapida = rapida("Floresta", "Caribe");
        var directa = rutaService.calcular("Floresta", "Caribe", CriterioRuta.MENOS_TRANSBORDOS).orElseThrow();
        assertTrue(rapida.minutos() < directa.minutos());
        assertTrue(rapida.transbordos() > directa.transbordos());
        assertEquals(0, directa.transbordos());
        assertTrue(directa.pasos().stream().allMatch(paso -> paso.linea().equals("Buses Línea O")));
    }
}
