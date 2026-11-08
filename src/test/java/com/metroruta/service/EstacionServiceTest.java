package com.metroruta.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.metroruta.model.AccionCierre;
import com.metroruta.model.Cierre;
import com.metroruta.model.Estacion;
import com.metroruta.repository.CierreRepository;
import com.metroruta.repository.EstacionRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EstacionServiceTest {

    private static final String NUEVA = "Estación de Prueba";
    private static final String RENOMBRADA = "Estación Renombrada";

    @Autowired
    private EstacionService estacionService;

    @Autowired
    private ConexionService conexionService;

    @Autowired
    private BusquedaService busquedaService;

    @Autowired
    private RutaService rutaService;

    @Autowired
    private EstacionRepository estacionRepository;

    @Autowired
    private CierreRepository cierreRepository;

    // borra lo que cree la prueba, este o no con el nombre original
    @AfterEach
    void limpiar() {
        for (String nombre : List.of(NUEVA, RENOMBRADA)) {
            if (estacionRepository.existsByNombreIgnoreCase(nombre)) {
                estacionService.eliminar(nombre);
            }
        }
    }

    @Test
    void crearUnaEstacionLaDejaEnElTrieYConPosicion() {
        Estacion creada = estacionService.crear("  " + NUEVA + "  ", null, null);
        assertEquals(NUEVA, creada.getNombre());
        assertTrue(creada.getPosX() != null && creada.getPosY() != null);
        assertTrue(busquedaService.buscar("Estación de P").contains(NUEVA));
    }

    @Test
    void unNombreRepetidoVacioLargoOConBarraEsError() {
        estacionService.crear(NUEVA, null, null);
        assertThrows(IllegalStateException.class, () -> estacionService.crear(NUEVA.toUpperCase(), null, null));
        assertThrows(IllegalStateException.class, () -> estacionService.crear("prado", null, null));
        assertThrows(IllegalArgumentException.class, () -> estacionService.crear("  ", null, null));
        assertThrows(IllegalArgumentException.class, () -> estacionService.crear(null, null, null));
        assertThrows(IllegalArgumentException.class, () -> estacionService.crear("x".repeat(61), null, null));
        assertThrows(IllegalArgumentException.class, () -> estacionService.crear("Mala|Estacion", null, null));
    }

    @Test
    void unaPosicionFueraDelDiagramaEsError() {
        assertThrows(IllegalArgumentException.class, () -> estacionService.crear(NUEVA, -1, 10));
        assertThrows(IllegalArgumentException.class, () -> estacionService.crear(NUEVA, 10, DiagramaService.ALTO + 1));
        assertFalse(estacionRepository.existsByNombreIgnoreCase(NUEVA));
    }

    @Test
    void editarCambiaElNombreEnElTrie() {
        estacionService.crear(NUEVA, 100, 700);
        estacionService.editar(NUEVA, RENOMBRADA, 120, 710);
        assertTrue(busquedaService.buscar("Estación Ren").contains(RENOMBRADA));
        assertFalse(busquedaService.buscar("Estación de P").contains(NUEVA));
        Estacion editada = estacionRepository.findByNombreIgnoreCase(RENOMBRADA).orElseThrow();
        assertEquals(120, editada.getPosX());
    }

    @Test
    void editarNoDejaUsarElNombreDeOtraEstacion() {
        estacionService.crear(NUEVA, null, null);
        assertThrows(IllegalStateException.class, () -> estacionService.editar(NUEVA, "Prado", null, null));
        assertThrows(IllegalArgumentException.class, () -> estacionService.editar("Atlantis", "Otra", null, null));
    }

    @Test
    void eliminarBorraLaEstacionSusConexionesYSusCierres() {
        Estacion creada = estacionService.crear(NUEVA, null, null);
        conexionService.crear("Prado", NUEVA, "Línea A", 3);
        cierreRepository.save(new Cierre(creada, AccionCierre.CERRAR, "prueba-test"));

        estacionService.eliminar(NUEVA);

        assertFalse(estacionRepository.existsByNombreIgnoreCase(NUEVA));
        assertFalse(busquedaService.buscar("Estación de P").contains(NUEVA));
        // Prado vuelve a tener solo sus dos vecinos
        assertEquals(12, rutaService.calcular("Prado", "Poblado", com.metroruta.model.CriterioRuta.RAPIDA)
                .orElseThrow().minutos());
        List<String> nombres = new ArrayList<>();
        estacionService.listar().forEach(e -> nombres.add(e.getNombre()));
        assertFalse(nombres.contains(NUEVA));
    }

    @Test
    void eliminarUnaEstacionQueNoExisteEsError() {
        assertThrows(IllegalArgumentException.class, () -> estacionService.eliminar("Atlantis"));
        assertThrows(IllegalArgumentException.class, () -> estacionService.eliminar(null));
    }
}
