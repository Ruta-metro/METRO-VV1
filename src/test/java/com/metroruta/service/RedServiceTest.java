package com.metroruta.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.metroruta.model.Estacion;
import com.metroruta.repository.EstacionRepository;
import com.metroruta.structures.Grafo;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RedServiceTest {

    @Autowired
    private RedService redService;

    @Autowired
    private EstacionRepository estacionRepository;

    @BeforeEach
    void armarGrafo() {
        redService.reconstruir();
    }

    private List<Grafo.Arista> vecinos(String nodo) {
        List<Grafo.Arista> lista = new ArrayList<>();
        for (Grafo.Arista a : redService.getGrafo().vecinos(nodo)) {
            lista.add(a);
        }
        return lista;
    }

    @Test
    void haceUnNodoPorCadaEstacionYLinea() {
        // 21 de la Linea A + 7 de la B + 9 del tranvia + 4 del Metrocable K
        assertTrue(redService.getGrafo().cantidadNodos() >= 41);
        assertTrue(redService.getGrafo().existeNodo(RedService.idNodo("Niquía", "Línea A")));
        assertTrue(redService.getGrafo().existeNodo(RedService.idNodo("San Javier", "Línea B")));
        assertTrue(redService.getGrafo().existeNodo(RedService.idNodo("Oriente", "Tranvía de Ayacucho")));
        assertTrue(redService.getGrafo().existeNodo(RedService.idNodo("Popular", "Metrocable Línea K")));
    }

    @Test
    void sanAntonioConectaSusTresLineasConTransbordo() {
        List<Grafo.Arista> vecinos = vecinos(RedService.idNodo("San Antonio", "Línea A"));
        Set<String> transbordos = new HashSet<>();
        int tramos = 0;
        for (Grafo.Arista a : vecinos) {
            if (a.transbordo()) {
                assertEquals(RedService.MINUTOS_TRANSBORDO, a.minutos());
                transbordos.add(a.destino());
            } else {
                tramos++;
            }
        }
        assertEquals(Set.of("San Antonio|Línea B", "San Antonio|Tranvía de Ayacucho"), transbordos);
        assertEquals(2, tramos); // Parque Berrio y Alpujarra
    }

    @Test
    void acevedoConectaLaLineaAConElMetrocableK() {
        Set<String> transbordos = new HashSet<>();
        for (Grafo.Arista a : vecinos(RedService.idNodo("Acevedo", "Línea A"))) {
            if (a.transbordo()) {
                transbordos.add(a.destino());
            }
        }
        assertEquals(Set.of("Acevedo|Metrocable Línea K"), transbordos);
    }

    @Test
    void unaEstacionIntermediaNoTieneTransbordos() {
        List<Grafo.Arista> vecinos = vecinos(RedService.idNodo("Prado", "Línea A"));
        assertEquals(2, vecinos.size());
        for (Grafo.Arista a : vecinos) {
            assertFalse(a.transbordo());
        }
    }

    @Test
    void losTramosVanEnLosDosSentidos() {
        List<Grafo.Arista> deNiquia = vecinos(RedService.idNodo("Niquía", "Línea A"));
        assertEquals(1, deNiquia.size()); // es la punta de la linea
        assertEquals("Bello|Línea A", deNiquia.get(0).destino());
        assertEquals(2, deNiquia.get(0).minutos());

        boolean vuelve = false;
        for (Grafo.Arista a : vecinos(RedService.idNodo("Bello", "Línea A"))) {
            vuelve = vuelve || a.destino().equals("Niquía|Línea A");
        }
        assertTrue(vuelve);
    }

    @Test
    void reconstruirDosVecesNoRepiteNodos() {
        int antes = redService.getGrafo().cantidadNodos();
        redService.reconstruir();
        assertEquals(antes, redService.getGrafo().cantidadNodos());
        assertEquals(4, vecinos(RedService.idNodo("San Antonio", "Línea A")).size());
    }

    @Test
    void estacionCerradaCierraTodosSusNodos() {
        Estacion sanAntonio = estacionRepository.findByNombreIgnoreCase("San Antonio").orElseThrow();
        try {
            sanAntonio.setCerrada(true);
            estacionRepository.save(sanAntonio);
            redService.reconstruir();
            for (String linea : new String[]{"Línea A", "Línea B", "Tranvía de Ayacucho"}) {
                assertTrue(redService.getGrafo().estaCerrado(RedService.idNodo("San Antonio", linea)));
            }
            assertFalse(redService.getGrafo().estaCerrado(RedService.idNodo("Prado", "Línea A")));
        } finally {
            // dejar la base de datos como estaba
            sanAntonio.setCerrada(false);
            estacionRepository.save(sanAntonio);
            redService.reconstruir();
        }
        assertFalse(redService.getGrafo().estaCerrado(RedService.idNodo("San Antonio", "Línea A")));
    }
}
