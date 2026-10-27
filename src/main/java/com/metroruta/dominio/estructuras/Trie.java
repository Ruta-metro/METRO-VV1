package com.metroruta.dominio.estructuras;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Trie {

    // cada nodo es una letra; si aqui termina una palabra, guardo el nombre original
    private static class Nodo {
        Map<Character, Nodo> hijos = new HashMap<>();
        String palabra;
    }

    private final Nodo raiz = new Nodo();

    // quita tildes y pasa a minusculas para comparar
    private static String normalizar(String texto) {
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase();
    }

    public void insertar(String palabra) {
        if (palabra == null || palabra.isBlank()) {
            throw new IllegalArgumentException("La palabra no puede estar vacia");
        }
        Nodo actual = raiz;
        for (char c : normalizar(palabra.trim()).toCharArray()) {
            actual = actual.hijos.computeIfAbsent(c, k -> new Nodo());
        }
        actual.palabra = palabra.trim();
    }

    // devuelve true si la palabra existia y se borro
    public boolean eliminar(String palabra) {
        if (palabra == null || palabra.isBlank()) {
            return false;
        }
        return eliminar(raiz, normalizar(palabra.trim()), 0);
    }

    private boolean eliminar(Nodo nodo, String clave, int i) {
        if (i == clave.length()) {
            if (nodo.palabra == null) {
                return false;
            }
            nodo.palabra = null;
            return true;
        }
        Nodo hijo = nodo.hijos.get(clave.charAt(i));
        if (hijo == null) {
            return false;
        }
        boolean borro = eliminar(hijo, clave, i + 1);
        // si el hijo quedo sin palabra y sin hijos, lo quito
        if (borro && hijo.palabra == null && hijo.hijos.isEmpty()) {
            nodo.hijos.remove(clave.charAt(i));
        }
        return borro;
    }

    // todas las palabras que empiezan con el prefijo, ordenadas
    public List<String> buscarPorPrefijo(String prefijo) {
        List<String> resultado = new ArrayList<>();
        if (prefijo == null) {
            return resultado;
        }
        Nodo actual = raiz;
        for (char c : normalizar(prefijo).toCharArray()) {
            actual = actual.hijos.get(c);
            if (actual == null) {
                return resultado;
            }
        }
        recolectar(actual, resultado);
        resultado.sort(String.CASE_INSENSITIVE_ORDER);
        return resultado;
    }

    private void recolectar(Nodo nodo, List<String> resultado) {
        if (nodo.palabra != null) {
            resultado.add(nodo.palabra);
        }
        for (Nodo hijo : nodo.hijos.values()) {
            recolectar(hijo, resultado);
        }
    }
}