package com.metroruta.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.metroruta.dto.Ruta;
import com.metroruta.dto.Segmento;
import com.metroruta.model.CriterioRuta;
import java.util.List;
import org.junit.jupiter.api.Test;

class DetalleServiceTest {

    private final DetalleService detalleService = new DetalleService();

    private Ruta ruta(List<Ruta.Paso> pasos, int minutos, int transbordos) {
        return new Ruta("o", "d", CriterioRuta.RAPIDA, pasos, minutos, transbordos);
    }

    @Test
    void unaRutaDeUnaSolaLineaEsUnSoloTramo() {
        Ruta ruta = ruta(List.of(new Ruta.Paso("Prado", "Línea A", 0, false),
                new Ruta.Paso("Hospital", "Línea A", 2, false)), 2, 0);
        List<Segmento> tramos = detalleService.armar(ruta);
        assertEquals(1, tramos.size());
        assertEquals(List.of("Prado", "Hospital"), tramos.get(0).estaciones());
        assertEquals(2, tramos.get(0).minutos());
        assertEquals(0, tramos.get(0).minutosTransbordoPrevio());
    }

    @Test
    void unTransbordoPartePorLaMitadYGuardaSusMinutos() {
        Ruta ruta = ruta(List.of(new Ruta.Paso("Cisneros", "Línea B", 0, false),
                new Ruta.Paso("San Antonio", "Línea B", 2, false),
                new Ruta.Paso("San Antonio", "Línea A", 6, true),
                new Ruta.Paso("Alpujarra", "Línea A", 8, false)), 8, 1);
        List<Segmento> tramos = detalleService.armar(ruta);
        assertEquals(2, tramos.size());
        assertEquals("Línea B", tramos.get(0).linea());
        assertEquals(2, tramos.get(0).minutos());
        assertEquals("Línea A", tramos.get(1).linea());
        assertEquals(List.of("San Antonio", "Alpujarra"), tramos.get(1).estaciones());
        assertEquals(2, tramos.get(1).minutos());
        assertEquals(4, tramos.get(1).minutosTransbordoPrevio());
    }

    @Test
    void unaRutaNulaOVaciaEsError() {
        assertThrows(IllegalArgumentException.class, () -> detalleService.armar(null));
        assertThrows(IllegalArgumentException.class, () -> detalleService.armar(ruta(List.of(), 0, 0)));
    }
}
