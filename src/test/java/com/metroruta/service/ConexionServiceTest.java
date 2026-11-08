package com.metroruta.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.metroruta.dto.Ruta;
import com.metroruta.model.Conexion;
import com.metroruta.model.CriterioRuta;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ConexionServiceTest {

    @Autowired
    private ConexionService conexionService;

    @Autowired
    private RutaService rutaService;

    // las conexiones que crea cada prueba, para borrarlas al final
    private final List<Long> creadas = new ArrayList<>();

    @AfterEach
    void limpiar() {
        for (Long id : creadas) {
            try {
                conexionService.eliminar(id);
            } catch (IllegalArgumentException e) {
                // ya la borro la prueba
            }
        }
        creadas.clear();
    }

    private Conexion crear(String origen, String destino, String linea, int minutos) {
        Conexion conexion = conexionService.crear(origen, destino, linea, minutos);
        creadas.add(conexion.getId());
        return conexion;
    }

    private Ruta ruta(String origen, String destino, CriterioRuta criterio) {
        return rutaService.calcular(origen, destino, criterio).orElseThrow();
    }

    @Test
    void conUnCicloLasDosRutasPuedenSerDistintas() {
        // la red real es un arbol: sin ciclos los dos criterios dan siempre lo mismo
        assertEquals(ruta("Cisneros", "Loyola", CriterioRuta.RAPIDA).pasos(),
                ruta("Cisneros", "Loyola", CriterioRuta.MENOS_TRANSBORDOS).pasos());

        // un tramo lento de la linea B entre Floresta y Loyola crea un ciclo
        crear("Floresta", "Loyola", "Línea B", 30);

        Ruta rapida = ruta("Cisneros", "Loyola", CriterioRuta.RAPIDA);
        Ruta pocos = ruta("Cisneros", "Loyola", CriterioRuta.MENOS_TRANSBORDOS);
        assertEquals(18, rapida.minutos());
        assertEquals(1, rapida.transbordos());
        assertEquals(36, pocos.minutos());
        assertEquals(0, pocos.transbordos());
        assertNotEquals(rapida.pasos(), pocos.pasos());
    }

    @Test
    void eliminarElTramoDevuelveLaRedComoEstaba() {
        Conexion conexion = crear("Floresta", "Loyola", "Línea B", 30);
        conexionService.eliminar(conexion.getId());
        assertEquals(ruta("Cisneros", "Loyola", CriterioRuta.RAPIDA).pasos(),
                ruta("Cisneros", "Loyola", CriterioRuta.MENOS_TRANSBORDOS).pasos());
    }

    @Test
    void cambiarLosMinutosCambiaLaRuta() {
        Conexion conexion = crear("Floresta", "Loyola", "Línea B", 30);
        conexionService.cambiarMinutos(conexion.getId(), 2);
        // ahora el camino por la B es el mas corto: 6 + 2 = 8 min
        assertEquals(8, ruta("Cisneros", "Loyola", CriterioRuta.RAPIDA).minutos());
    }

    @Test
    void noSePuedeRepetirUnTramoNiUnirUnaEstacionConSigoMisma() {
        crear("Floresta", "Loyola", "Línea B", 30);
        assertThrows(IllegalStateException.class, () -> conexionService.crear("Loyola", "Floresta", "Línea B", 5));
        assertThrows(IllegalArgumentException.class, () -> conexionService.crear("Prado", "prado", "Línea A", 5));
    }

    @Test
    void losDatosInvalidosSeRechazan() {
        assertThrows(IllegalArgumentException.class, () -> conexionService.crear("Prado", "Hospital", "Línea A", 0));
        assertThrows(IllegalArgumentException.class, () -> conexionService.crear("Prado", "Hospital", "Línea A", 121));
        assertThrows(IllegalArgumentException.class, () -> conexionService.crear("Atlantis", "Hospital", "Línea A", 5));
        assertThrows(IllegalArgumentException.class, () -> conexionService.crear("Prado", "Hospital", "Línea Z", 5));
        assertThrows(IllegalArgumentException.class, () -> conexionService.crear(null, "Hospital", "Línea A", 5));
        assertThrows(IllegalArgumentException.class, () -> conexionService.cambiarMinutos(null, 5));
        assertThrows(IllegalArgumentException.class, () -> conexionService.eliminar(-5L));
    }

    @Test
    void listaLasConexionesYLosNombres() {
        assertTrue(conexionService.listar().size() >= 37);
        assertTrue(conexionService.nombresDeLineas().contains("Línea A"));
        assertTrue(conexionService.nombresDeEstaciones().contains("Prado"));
    }
}
