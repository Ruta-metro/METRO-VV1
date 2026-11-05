package com.metroruta.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.metroruta.model.Estacion;
import com.metroruta.repository.EstacionRepository;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BusquedaServiceTest {

    @Mock
    private EstacionRepository estacionRepository;

    private BusquedaService servicio;

    private List<Estacion> estaciones(String... nombres) {
        return Arrays.stream(nombres).map(Estacion::new).toList();
    }

    @BeforeEach
    void preparar() {
        when(estacionRepository.findAll()).thenReturn(
                estaciones("San José", "Itagüí", "Sabaneta", "San Javier", "Poblado", "San Antonio"));
        servicio = new BusquedaService(estacionRepository);
        servicio.reconstruir();
    }

    @Test
    void buscaPorPrefijo() {
        assertEquals(List.of("San Antonio", "San Javier", "San José"), servicio.buscar("san"));
    }

    @Test
    void ignoraMayusculasYTildes() {
        assertEquals(List.of("Itagüí"), servicio.buscar("ITAGUI"));
        assertEquals(List.of("San José"), servicio.buscar("san jose"));
    }

    @Test
    void losEmpatesSalenEnOrdenAlfabetico() {
        assertEquals(List.of("Sabaneta", "San Antonio", "San Javier", "San José"), servicio.buscar("s"));
    }

    @Test
    void sinCoincidenciasDevuelveListaVacia() {
        assertTrue(servicio.buscar("zzz").isEmpty());
    }

    @Test
    void textoNuloDevuelveListaVacia() {
        assertTrue(servicio.buscar(null).isEmpty());
    }

    @Test
    void textoVacioDevuelveTodasLasEstaciones() {
        assertEquals(6, servicio.buscar("").size());
    }

    @Test
    void ignoraLosEspaciosAlrededor() {
        assertEquals(3, servicio.buscar("  san ").size());
    }

    @Test
    void reconstruirReflejaLosCambios() {
        assertTrue(servicio.buscar("santa").isEmpty());
        when(estacionRepository.findAll()).thenReturn(estaciones("Santa Lucía", "Itagüí"));
        servicio.reconstruir();
        assertEquals(List.of("Santa Lucía"), servicio.buscar("santa"));
        assertTrue(servicio.buscar("pob").isEmpty());
    }
}