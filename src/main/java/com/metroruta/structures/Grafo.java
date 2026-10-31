package com.metroruta.structures;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Grafo {

    // un tramo hacia otro nodo: destino, minutos y si es un transbordo
    public record Arista(String destino, int minutos, boolean transbordo) {
    }

    // tabla hash: nodo -> lista enlazada con sus vecinos (lista de adyacencia)
    private final Map<String, ListaEnlazada<Arista>> adyacencia = new HashMap<>();

    // nodos cerrados, Dijkstra los va a saltar
    private final Set<String> cerrados = new HashSet<>();

    public void agregarNodo(String id) {
        adyacencia.putIfAbsent(id, new ListaEnlazada<>());
    }

    public boolean existeNodo(String id) {
        return adyacencia.containsKey(id);
    }

    public int cantidadNodos() {
        return adyacencia.size();
    }

    // conecta en los dos sentidos (el metro va y viene)
    public void conectar(String a, String b, int minutos, boolean transbordo) {
        if (minutos <= 0) {
            throw new IllegalArgumentException("Los minutos deben ser mayores que 0");
        }
        if (!existeNodo(a) || !existeNodo(b)) {
            throw new IllegalArgumentException("El nodo no existe");
        }
        adyacencia.get(a).agregar(new Arista(b, minutos, transbordo));
        adyacencia.get(b).agregar(new Arista(a, minutos, transbordo));
    }

    public Iterable<Arista> vecinos(String id) {
        ListaEnlazada<Arista> lista = adyacencia.get(id);
        if (lista == null) {
            throw new IllegalArgumentException("El nodo no existe");
        }
        return lista;
    }

    // quita la conexion entre a y b en los dos sentidos
    public void desconectar(String a, String b) {
        if (existeNodo(a)) {
            adyacencia.get(a).eliminarSi(x -> x.destino().equals(b));
        }
        if (existeNodo(b)) {
            adyacencia.get(b).eliminarSi(x -> x.destino().equals(a));
        }
    }

    // borra el nodo y todas las aristas que llegaban a el
    public void eliminarNodo(String id) {
        adyacencia.remove(id);
        cerrados.remove(id);
        for (ListaEnlazada<Arista> lista : adyacencia.values()) {
            lista.eliminarSi(x -> x.destino().equals(id));
        }
    }

    public void cerrar(String id) {
        if (!existeNodo(id)) {
            throw new IllegalArgumentException("El nodo no existe");
        }
        cerrados.add(id);
    }

    public void abrir(String id) {
        cerrados.remove(id);
    }

    public boolean estaCerrado(String id) {
        return cerrados.contains(id);
    }
}
