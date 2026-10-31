package com.metroruta.structures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ListaEnlazadaTest {

    private List<Integer> aList(ListaEnlazada<Integer> lista) {
        List<Integer> res = new ArrayList<>();
        for (int x : lista) {
            res.add(x);
        }
        return res;
    }

    @Test
    void nuevaListaEstaVacia() {
        ListaEnlazada<Integer> lista = new ListaEnlazada<>();
        assertTrue(lista.estaVacia());
        assertEquals(0, lista.tamanio());
    }

    @Test
    void agregaYRecorreEnOrden() {
        ListaEnlazada<Integer> lista = new ListaEnlazada<>();
        lista.agregar(1);
        lista.agregar(2);
        lista.agregar(3);
        assertEquals(List.of(1, 2, 3), aList(lista));
        assertEquals(3, lista.tamanio());
    }

    @Test
    void eliminaPrimeroMedioYUltimo() {
        ListaEnlazada<Integer> lista = new ListaEnlazada<>();
        for (int i = 1; i <= 5; i++) {
            lista.agregar(i);
        }
        assertEquals(1, lista.eliminarSi(x -> x == 1));
        assertEquals(1, lista.eliminarSi(x -> x == 3));
        assertEquals(1, lista.eliminarSi(x -> x == 5));
        assertEquals(List.of(2, 4), aList(lista));
    }

    @Test
    void sigueAgregandoDespuesDeBorrarElUltimo() {
        ListaEnlazada<Integer> lista = new ListaEnlazada<>();
        lista.agregar(1);
        lista.agregar(2);
        lista.eliminarSi(x -> x == 2);
        lista.agregar(9);
        assertEquals(List.of(1, 9), aList(lista));
    }

    @Test
    void borrarTodoDejaLaListaVacia() {
        ListaEnlazada<Integer> lista = new ListaEnlazada<>();
        lista.agregar(1);
        lista.agregar(2);
        assertEquals(2, lista.eliminarSi(x -> true));
        assertTrue(lista.estaVacia());
        lista.agregar(7);
        assertEquals(List.of(7), aList(lista));
    }
}
