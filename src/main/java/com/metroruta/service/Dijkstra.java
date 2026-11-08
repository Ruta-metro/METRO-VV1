package com.metroruta.service;

import com.metroruta.dto.Ruta;
import com.metroruta.model.CriterioRuta;
import com.metroruta.structures.Grafo;
import com.metroruta.structures.MinHeap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// camino de menor costo entre dos estaciones, usando el MinHeap propio
public final class Dijkstra {

    // con MENOS_TRANSBORDOS cada transbordo pesa mas que cualquier recorrido de la red
    static final int PESO_TRANSBORDO = 1000;

    // un nodo pendiente en el heap con lo que cuesta llegar hasta el
    private record Entrada(String nodo, int costo) implements Comparable<Entrada> {
        @Override
        public int compareTo(Entrada otra) {
            int porCosto = Integer.compare(costo, otra.costo);
            // en empate de costo se desempata por nombre para que el resultado siempre sea el mismo
            return porCosto != 0 ? porCosto : nodo.compareTo(otra.nodo);
        }
    }

    private Dijkstra() {
    }

    private static int peso(Grafo.Arista arista, CriterioRuta criterio) {
        if (criterio == CriterioRuta.MENOS_TRANSBORDOS && arista.transbordo()) {
            return PESO_TRANSBORDO + arista.minutos();
        }
        return arista.minutos();
    }

    // devuelve vacio si no hay camino (estacion cerrada o red cortada)
    public static Optional<Ruta> calcular(Grafo grafo, String origen, String destino, CriterioRuta criterio) {
        if (origen.equals(destino)) {
            throw new IllegalArgumentException("El origen y el destino deben ser distintos");
        }

        Map<String, Integer> costo = new HashMap<>();
        Map<String, String> anterior = new HashMap<>();
        MinHeap<Entrada> pendientes = new MinHeap<>();

        // se puede salir por cualquier linea de la estacion de origen sin pagar transbordo
        for (String nodo : grafo.nodos()) {
            if (RedService.estacionDe(nodo).equals(origen) && !grafo.estaCerrado(nodo)) {
                costo.put(nodo, 0);
                pendientes.insertar(new Entrada(nodo, 0));
            }
        }

        String llegada = null;
        while (!pendientes.estaVacio()) {
            Entrada actual = pendientes.extraerMinimo();
            // el heap no baja costos: si ya hay un camino mejor a este nodo, esta entrada es vieja
            if (actual.costo() > costo.get(actual.nodo())) {
                continue;
            }
            if (RedService.estacionDe(actual.nodo()).equals(destino)) {
                llegada = actual.nodo();
                break;
            }
            for (Grafo.Arista arista : grafo.vecinos(actual.nodo())) {
                if (grafo.estaCerrado(arista.destino())) {
                    continue;
                }
                int nuevo = actual.costo() + peso(arista, criterio);
                Integer previo = costo.get(arista.destino());
                if (previo == null || nuevo < previo) {
                    costo.put(arista.destino(), nuevo);
                    anterior.put(arista.destino(), actual.nodo());
                    pendientes.insertar(new Entrada(arista.destino(), nuevo));
                }
            }
        }

        if (llegada == null) {
            return Optional.empty();
        }
        return Optional.of(armarRuta(grafo, anterior, llegada, origen, destino, criterio));
    }

    // recorre los anteriores desde la llegada hasta el origen y arma los pasos
    private static Ruta armarRuta(Grafo grafo, Map<String, String> anterior, String llegada,
                                  String origen, String destino, CriterioRuta criterio) {
        List<String> nodos = new ArrayList<>();
        for (String nodo = llegada; nodo != null; nodo = anterior.get(nodo)) {
            nodos.add(nodo);
        }
        Collections.reverse(nodos);

        List<Ruta.Paso> pasos = new ArrayList<>();
        int minutos = 0;
        int transbordos = 0;
        for (int i = 0; i < nodos.size(); i++) {
            boolean transbordo = false;
            if (i > 0) {
                Grafo.Arista tramo = tramoEntre(grafo, nodos.get(i - 1), nodos.get(i));
                minutos += tramo.minutos();
                transbordo = tramo.transbordo();
                if (transbordo) {
                    transbordos++;
                }
            }
            pasos.add(new Ruta.Paso(RedService.estacionDe(nodos.get(i)), RedService.lineaDe(nodos.get(i)),
                    minutos, transbordo));
        }
        return new Ruta(origen, destino, criterio, pasos, minutos, transbordos);
    }

    // el tramo mas corto que une dos nodos vecinos
    private static Grafo.Arista tramoEntre(Grafo grafo, String desde, String hasta) {
        Grafo.Arista mejor = null;
        for (Grafo.Arista arista : grafo.vecinos(desde)) {
            if (arista.destino().equals(hasta) && (mejor == null || arista.minutos() < mejor.minutos())) {
                mejor = arista;
            }
        }
        return mejor;
    }
}
