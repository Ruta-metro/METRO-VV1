package com.metroruta.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.metroruta.dto.Ruta;
import com.metroruta.model.CriterioRuta;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class LlegadaServiceTest {

    private final LlegadaService llegadaService = new LlegadaService();

    private Ruta ruta(int minutos) {
        return new Ruta("o", "d", CriterioRuta.RAPIDA, List.of(), minutos, 0);
    }

    @Test
    void sumaLosMinutosALaHoraDeSalida() {
        LocalDateTime salida = LocalDateTime.of(2026, 11, 10, 10, 0);
        LocalDateTime llegada = llegadaService.horaLlegada(salida, ruta(12));
        assertEquals(LocalDateTime.of(2026, 11, 10, 10, 12), llegada);
        assertEquals("10:12", llegadaService.formatear(salida, llegada));
    }

    @Test
    void siPasaDeMedianocheAvisaQueEsElDiaSiguiente() {
        LocalDateTime salida = LocalDateTime.of(2026, 11, 10, 23, 50);
        LocalDateTime llegada = llegadaService.horaLlegada(salida, ruta(20));
        assertEquals("00:10 (día siguiente)", llegadaService.formatear(salida, llegada));
    }

    @Test
    void sinSalidaOSinRutaEsError() {
        assertThrows(IllegalArgumentException.class, () -> llegadaService.horaLlegada(null, ruta(5)));
        assertThrows(IllegalArgumentException.class,
                () -> llegadaService.horaLlegada(LocalDateTime.now(), null));
    }
}
