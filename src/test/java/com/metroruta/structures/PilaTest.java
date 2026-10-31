package com.metroruta.structures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

class PilaTest {

    @Test
    void nuevaPilaEstaVacia() {
        Pila<String> pila = new Pila<>();
        assertTrue(pila.estaVacia());
        assertEquals(0, pila.tamanio());
    }

    @Test
    void desapilaEnOrdenInverso() {
        Pila<Integer> pila = new Pila<>();
        pila.apilar(1);
        pila.apilar(2);
        pila.apilar(3);
        assertEquals(3, pila.desapilar());
        assertEquals(2, pila.desapilar());
        assertEquals(1, pila.desapilar());
        assertTrue(pila.estaVacia());
    }

    @Test
    void cimaNoSacaElElemento() {
        Pila<String> pila = new Pila<>();
        pila.apilar("A");
        pila.apilar("B");
        assertEquals("B", pila.cima());
        assertEquals(2, pila.tamanio());
    }

    @Test
    void vaciaLanzaExcepcion() {
        Pila<String> pila = new Pila<>();
        assertThrows(NoSuchElementException.class, pila::desapilar);
        assertThrows(NoSuchElementException.class, pila::cima);
    }

    @Test
    void sirveDespuesDeVaciarse() {
        Pila<Integer> pila = new Pila<>();
        pila.apilar(1);
        pila.desapilar();
        pila.apilar(5);
        assertEquals(5, pila.cima());
        assertEquals(1, pila.tamanio());
    }
}