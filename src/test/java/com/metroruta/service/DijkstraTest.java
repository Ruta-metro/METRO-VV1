package com.metroruta.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.metroruta.dto.Ruta;
import com.metroruta.model.CriterioRuta;
import com.metroruta.structures.Grafo;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

// pruebas del algoritmo con grafos chicos armados a mano
class DijkstraTest {

    private static String n(String estacion, String linea) {
        return RedService.idNodo(estacion, linea);
    }

    // X a Z se puede ir por la linea L1 (lenta, sin transbordo) o por L2 y L3 (rapida, con un transbordo en W)
    private Grafo grafoConDosCaminos() {
        Grafo g = new Grafo();
        for (String nodo : new String[]{n("X", "L1"), n("Y", "L1"), n("Z", "L1"),
                n("X", "L2"), n("W", "L2"), n("W", "L3"), n("Z", "L3")}) {
            g.agregarNodo(nodo);
        }
        g.conectar(n("X", "L1"), n("Y", "L1"), 12, false);
        g.conectar(n("Y", "L1"), n("Z", "L1"), 12, false);
        g.conectar(n("X", "L2"), n("W", "L2"), 2, false);
        g.conectar(n("W", "L2"), n("W", "L3"), 4, true);
        g.conectar(n("W", "L3"), n("Z", "L3"), 2, false);
        return g;
    }

    private List<String> estaciones(Ruta ruta) {
        List<String> lista = new ArrayList<>();
        for (Ruta.Paso paso : ruta.pasos()) {
            lista.add(paso.estacion());
        }
        return lista;
    }

    @Test
    void laMasRapidaPrefiereElCaminoConTransbordoSiEsMasCorto() {
        Ruta ruta = Dijkstra.calcular(grafoConDosCaminos(), "X", "Z", CriterioRuta.RAPIDA).orElseThrow();
        assertEquals(8, ruta.minutos());
        assertEquals(1, ruta.transbordos());
        assertEquals(List.of("X", "W", "W", "Z"), estaciones(ruta));
    }

    @Test
    void menosTransbordosPrefiereElCaminoSinTransbordoAunqueSeaMasLento() {
        Ruta ruta = Dijkstra.calcular(grafoConDosCaminos(), "X", "Z", CriterioRuta.MENOS_TRANSBORDOS).orElseThrow();
        assertEquals(24, ruta.minutos());
        assertEquals(0, ruta.transbordos());
        assertEquals(List.of("X", "Y", "Z"), estaciones(ruta));
    }

    @Test
    void lasDosRutasDifierenPeroMenosTransbordosNuncaTieneMasTransbordos() {
        Grafo g = grafoConDosCaminos();
        Ruta rapida = Dijkstra.calcular(g, "X", "Z", CriterioRuta.RAPIDA).orElseThrow();
        Ruta pocos = Dijkstra.calcular(g, "X", "Z", CriterioRuta.MENOS_TRANSBORDOS).orElseThrow();
        assertTrue(!estaciones(rapida).equals(estaciones(pocos)));
        assertTrue(pocos.transbordos() <= rapida.transbordos());
        assertTrue(rapida.minutos() <= pocos.minutos());
    }

    @Test
    void elTiempoTotalEsLaSumaDeLosTramosDeLosPasos() {
        Ruta ruta = Dijkstra.calcular(grafoConDosCaminos(), "X", "Z", CriterioRuta.RAPIDA).orElseThrow();
        assertEquals(0, ruta.pasos().get(0).minutosAcumulados());
        assertEquals(ruta.minutos(), ruta.pasos().get(ruta.pasos().size() - 1).minutosAcumulados());
    }

    @Test
    void sinCaminoDevuelveVacio() {
        Grafo g = new Grafo();
        g.agregarNodo(n("A", "L1"));
        g.agregarNodo(n("B", "L2")); // dos lineas que no se tocan
        assertEquals(Optional.empty(), Dijkstra.calcular(g, "A", "B", CriterioRuta.RAPIDA));
    }

    @Test
    void unaEstacionCerradaEnMedioCortaLaRuta() {
        Grafo g = new Grafo();
        g.agregarNodo(n("A", "L1"));
        g.agregarNodo(n("B", "L1"));
        g.agregarNodo(n("C", "L1"));
        g.conectar(n("A", "L1"), n("B", "L1"), 2, false);
        g.conectar(n("B", "L1"), n("C", "L1"), 2, false);
        assertTrue(Dijkstra.calcular(g, "A", "C", CriterioRuta.RAPIDA).isPresent());
        g.cerrar(n("B", "L1"));
        assertTrue(Dijkstra.calcular(g, "A", "C", CriterioRuta.RAPIDA).isEmpty());
    }

    @Test
    void conOrigenODestinoCerradoNoHayRuta() {
        Grafo g = new Grafo();
        g.agregarNodo(n("A", "L1"));
        g.agregarNodo(n("B", "L1"));
        g.conectar(n("A", "L1"), n("B", "L1"), 2, false);
        g.cerrar(n("B", "L1"));
        assertTrue(Dijkstra.calcular(g, "A", "B", CriterioRuta.RAPIDA).isEmpty());
        assertTrue(Dijkstra.calcular(g, "B", "A", CriterioRuta.RAPIDA).isEmpty());
    }

    @Test
    void origenIgualADestinoEsError() {
        assertThrows(IllegalArgumentException.class,
                () -> Dijkstra.calcular(grafoConDosCaminos(), "X", "X", CriterioRuta.RAPIDA));
    }

    @Test
    void conDosCaminosDelMismoTiempoElResultadoEsSiempreElMismo() {
        // A a D por B o por C, los dos de 4 minutos
        Grafo g = new Grafo();
        for (String e : new String[]{"A", "B", "C", "D"}) {
            g.agregarNodo(n(e, "L1"));
        }
        g.conectar(n("A", "L1"), n("B", "L1"), 2, false);
        g.conectar(n("A", "L1"), n("C", "L1"), 2, false);
        g.conectar(n("B", "L1"), n("D", "L1"), 2, false);
        g.conectar(n("C", "L1"), n("D", "L1"), 2, false);
        Ruta primera = Dijkstra.calcular(g, "A", "D", CriterioRuta.RAPIDA).orElseThrow();
        Ruta segunda = Dijkstra.calcular(g, "A", "D", CriterioRuta.RAPIDA).orElseThrow();
        assertEquals(4, primera.minutos());
        assertEquals(estaciones(primera), estaciones(segunda));
    }
}
