package com.metroruta.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.metroruta.model.Conexion;
import com.metroruta.repository.ConexionRepository;
import com.metroruta.repository.EstacionRepository;
import com.metroruta.repository.LineaRepository;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CargaInicialTest {

    @Autowired
    private CargaInicial cargaInicial;

    @Autowired
    private EstacionRepository estacionRepository;

    @Autowired
    private LineaRepository lineaRepository;

    @Autowired
    private ConexionRepository conexionRepository;

    // nombres de las lineas por las que pasa una estacion, segun sus conexiones
    private Set<String> lineasDe(String estacion) {
        Set<String> lineas = new HashSet<>();
        for (Conexion c : conexionRepository.findAll()) {
            if (c.getOrigen().getNombre().equals(estacion) || c.getDestino().getNombre().equals(estacion)) {
                lineas.add(c.getLinea().getNombre());
            }
        }
        return lineas;
    }

    @Test
    void cargaLasCuatroLineasYSusEstaciones() {
        for (String linea : new String[]{"Línea A", "Línea B", "Tranvía de Ayacucho", "Metrocable Línea K"}) {
            assertTrue(lineaRepository.findByNombre(linea).isPresent(), "falta " + linea);
        }
        // una de cada punta de linea
        for (String estacion : new String[]{"Niquía", "La Estrella", "San Javier", "Oriente", "Santo Domingo Savio"}) {
            assertTrue(estacionRepository.existsByNombreIgnoreCase(estacion), "falta " + estacion);
        }
    }

    @Test
    void sanAntonioEsTransbordoDeTresLineas() {
        assertEquals(Set.of("Línea A", "Línea B", "Tranvía de Ayacucho"), lineasDe("San Antonio"));
    }

    @Test
    void acevedoUneLaLineaAConElMetrocableK() {
        assertEquals(Set.of("Línea A", "Metrocable Línea K"), lineasDe("Acevedo"));
    }

    @Test
    void unaEstacionIntermediaSoloTieneUnaLinea() {
        assertEquals(Set.of("Línea A"), lineasDe("Prado"));
    }

    @Test
    void todosLosTramosTienenMinutosPositivos() {
        for (Conexion c : conexionRepository.findAll()) {
            assertTrue(c.getMinutos() > 0, "tramo sin minutos: " + c);
        }
    }

    @Test
    void noDuplicaLaRedSiYaEstaCargada() {
        long estacionesAntes = estacionRepository.count();
        long conexionesAntes = conexionRepository.count();
        assertFalse(cargaInicial.cargarSiVacia());
        assertEquals(estacionesAntes, estacionRepository.count());
        assertEquals(conexionesAntes, conexionRepository.count());
    }
}
