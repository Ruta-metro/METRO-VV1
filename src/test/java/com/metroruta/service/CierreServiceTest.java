package com.metroruta.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.metroruta.model.CriterioRuta;
import com.metroruta.model.Estacion;
import com.metroruta.repository.CierreRepository;
import com.metroruta.repository.EstacionRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CierreServiceTest {

    private static final String USUARIO = "prueba-test";

    @Autowired
    private CierreService cierreService;

    @Autowired
    private RutaService rutaService;

    @Autowired
    private RedService redService;

    @Autowired
    private EstacionRepository estacionRepository;

    @Autowired
    private CierreRepository cierreRepository;

    @Autowired
    private DifusorEventos difusor;

    @BeforeEach
    void empezarLimpio() {
        dejarTodoAbierto();
    }

    @AfterEach
    void dejarTodoComoEstaba() {
        dejarTodoAbierto();
        cierreRepository.deleteAll(cierreRepository.findAll().stream()
                .filter(c -> USUARIO.equals(c.getUsuario())).toList());
    }

    private void dejarTodoAbierto() {
        List<Estacion> cambiadas = new ArrayList<>();
        for (Estacion estacion : estacionRepository.findAll()) {
            if (estacion.isCerrada()) {
                estacion.setCerrada(false);
                cambiadas.add(estacion);
            }
        }
        estacionRepository.saveAll(cambiadas);
        cierreService.vaciarHistorial();
        redService.reconstruir();
    }

    private boolean hayRuta(String origen, String destino) {
        return rutaService.calcular(origen, destino, CriterioRuta.RAPIDA).isPresent();
    }

    @Test
    void cerrarLaUnicaConexionAArviImpideLlegarYReabrirLaRestaura() {
        assertTrue(hayRuta("Arví", "Popular"));
        cierreService.cerrar("Santo Domingo Savio", USUARIO);
        assertFalse(hayRuta("Arví", "Popular"));
        assertEquals(List.of("Santo Domingo Savio"), cierreService.cerradas());
        cierreService.reabrir("santo domingo savio", USUARIO);
        assertTrue(hayRuta("Arví", "Popular"));
        assertTrue(cierreService.cerradas().isEmpty());
    }

    @Test
    void cerrarUnaYaCerradaOReabrirUnaAbiertaEsError() {
        assertThrows(IllegalStateException.class, () -> cierreService.reabrir("Prado", USUARIO));
        cierreService.cerrar("Prado", USUARIO);
        assertThrows(IllegalStateException.class, () -> cierreService.cerrar("Prado", USUARIO));
    }

    @Test
    void unaEstacionQueNoExisteOUnDatoVacioEsError() {
        assertThrows(IllegalArgumentException.class, () -> cierreService.cerrar("Atlantis", USUARIO));
        assertThrows(IllegalArgumentException.class, () -> cierreService.cerrar("", USUARIO));
        assertThrows(IllegalArgumentException.class, () -> cierreService.cerrar(null, USUARIO));
        assertThrows(IllegalArgumentException.class, () -> cierreService.cerrar("Prado", " "));
    }

    @Test
    void deshacerVuelveAtrasEnOrdenInverso() {
        cierreService.cerrar("Prado", USUARIO);
        cierreService.cerrar("Hospital", USUARIO);
        assertTrue(cierreService.puedeDeshacer());

        cierreService.deshacer(USUARIO);
        assertEquals(List.of("Prado"), cierreService.cerradas());
        cierreService.deshacer(USUARIO);
        assertTrue(cierreService.cerradas().isEmpty());
        assertFalse(cierreService.puedeDeshacer());
    }

    @Test
    void deshacerUnaReaperturaLaVuelveACerrar() {
        cierreService.cerrar("Prado", USUARIO);
        cierreService.reabrir("Prado", USUARIO);
        cierreService.deshacer(USUARIO);
        assertEquals(List.of("Prado"), cierreService.cerradas());
    }

    @Test
    void deshacerConLaPilaVaciaEsError() {
        assertFalse(cierreService.puedeDeshacer());
        assertThrows(IllegalStateException.class, () -> cierreService.deshacer(USUARIO));
    }

    @Test
    void cerrarUnaEstacionAvisaALasSesiones() {
        List<String> avisos = new ArrayList<>();
        Runnable cancelar = difusor.suscribir(avisos::add);
        try {
            cierreService.cerrar("Prado", USUARIO);
        } finally {
            cancelar.run();
        }
        assertEquals(List.of("La estación Prado fue cerrada"), avisos);
    }

    @Test
    void universidadDeMedellinANiquiaEncuentraDesvioConTresCierres() {
        // Regresión del recorrido reportado: los cierres se aplican en vivo y en ese orden.
        for (String cerrada : List.of("Estadio", "San Antonio", "Cisneros")) {
            cierreService.cerrar(cerrada, USUARIO);
            for (CriterioRuta criterio : CriterioRuta.values()) {
                for (boolean inversa : List.of(false, true)) {
                    var ruta = rutaService.calcular(inversa ? "Niquía" : "Universidad de Medellín",
                            inversa ? "Universidad de Medellín" : "Niquía", criterio).orElseThrow();
                    assertTrue(ruta.pasos().stream().noneMatch(p -> cierreService.cerradas().contains(p.estacion())));
                }
            }
        }
        var desvio = rutaService.calcular("Universidad de Medellín", "Niquía", CriterioRuta.RAPIDA).orElseThrow();
        assertEquals(62, desvio.minutos());
        assertEquals(2, desvio.transbordos());
        assertEquals(List.of("Buses Línea 2", "Metroplús Línea 1", "Línea A"),
                desvio.pasos().stream().map(p -> p.linea()).distinct().toList());

        // Si se corta también la alternativa por Palos Verdes, la línea O sigue siendo válida.
        cierreService.cerrar("Palos Verdes", USUARIO);
        var porCaribe = rutaService.calcular("Universidad de Medellín", "Niquía", CriterioRuta.RAPIDA).orElseThrow();
        assertTrue(porCaribe.pasos().stream().anyMatch(p -> p.linea().equals("Buses Línea O")));
        assertTrue(porCaribe.pasos().stream().noneMatch(p -> cierreService.cerradas().contains(p.estacion())));

        cierreService.deshacer(USUARIO);
        assertEquals(desvio.pasos(), rutaService.calcular("Universidad de Medellín", "Niquía", CriterioRuta.RAPIDA)
                .orElseThrow().pasos());
    }
}
