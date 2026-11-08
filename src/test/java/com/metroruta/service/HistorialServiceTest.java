package com.metroruta.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.metroruta.dto.Ruta;
import com.metroruta.model.CriterioRuta;
import com.metroruta.model.HistorialRuta;
import com.metroruta.repository.HistorialRutaRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HistorialServiceTest {

    @Autowired
    private HistorialService historialService;

    @Autowired
    private RutaService rutaService;

    @Autowired
    private HistorialRutaRepository historialRepository;

    // lo que guarda cada prueba, para borrarlo al final y no ensuciar la base de datos
    private final List<HistorialRuta> creados = new ArrayList<>();

    @AfterEach
    void limpiar() {
        historialRepository.deleteAll(creados);
        creados.clear();
    }

    private Ruta ruta(String origen, String destino) {
        return rutaService.calcular(origen, destino, CriterioRuta.RAPIDA).orElseThrow();
    }

    @Test
    void guardaLaRutaDelUsuario() {
        HistorialRuta registro = historialService.guardar("usuario", ruta("Prado", "Poblado"));
        creados.add(registro);
        assertEquals("Prado", registro.getOrigen());
        assertEquals(12, registro.getMinutos());
        assertTrue(historialService.ultimas("usuario").contains(registro));
    }

    @Test
    void cadaUsuarioSoloVeSusRutas() {
        HistorialRuta registro = historialService.guardar("usuario", ruta("Prado", "Poblado"));
        creados.add(registro);
        assertFalse(historialService.ultimas("admin").contains(registro));
    }

    @Test
    void devuelveComoMaximoLasUltimas20() {
        for (int i = 0; i < 25; i++) {
            creados.add(historialService.guardar("usuario", ruta("Prado", "Hospital")));
        }
        List<HistorialRuta> ultimas = historialService.ultimas("usuario");
        assertEquals(20, ultimas.size());
        for (int i = 1; i < ultimas.size(); i++) {
            assertFalse(ultimas.get(i).getFecha().isAfter(ultimas.get(i - 1).getFecha()));
        }
    }

    @Test
    void unUsuarioQueNoExisteOUnaRutaNulaEsError() {
        assertThrows(IllegalArgumentException.class, () -> historialService.guardar("fantasma", ruta("Prado", "Poblado")));
        assertThrows(IllegalArgumentException.class, () -> historialService.guardar(null, ruta("Prado", "Poblado")));
        assertThrows(IllegalArgumentException.class, () -> historialService.guardar("usuario", null));
        assertThrows(IllegalArgumentException.class, () -> historialService.ultimas("fantasma"));
    }
}
