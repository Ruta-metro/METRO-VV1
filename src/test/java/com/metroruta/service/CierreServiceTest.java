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
    void cerrarUnaEstacionCortaLaRutaYReabrirlaLaDevuelve() {
        assertTrue(hayRuta("Hospital", "Parque Berrío"));
        cierreService.cerrar("Prado", USUARIO);
        assertFalse(hayRuta("Hospital", "Parque Berrío"));
        assertEquals(List.of("Prado"), cierreService.cerradas());
        cierreService.reabrir("prado", USUARIO);
        assertTrue(hayRuta("Hospital", "Parque Berrío"));
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
}
