package com.metroruta.dominio.estructuras;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.NoSuchElementException;
import java.util.Random;
import org.junit.jupiter.api.Test;

class MinHeapTest {

    @Test
    void nuevoHeapEstaVacio() {
        MinHeap<Integer> heap = new MinHeap<>();
        assertTrue(heap.estaVacio());
        assertEquals(0, heap.tamanio());
    }

    @Test
    void extraeEnOrdenAscendente() {
        MinHeap<Integer> heap = new MinHeap<>();
        for (int x : new int[]{5, 3, 8, 1, 9, 2}) {
            heap.insertar(x);
        }
        for (int esperado : new int[]{1, 2, 3, 5, 8, 9}) {
            assertEquals(esperado, heap.extraerMinimo());
        }
        assertTrue(heap.estaVacio());
    }

    @Test
    void manejaRepetidos() {
        MinHeap<Integer> heap = new MinHeap<>();
        heap.insertar(4);
        heap.insertar(4);
        heap.insertar(1);
        assertEquals(1, heap.extraerMinimo());
        assertEquals(4, heap.extraerMinimo());
        assertEquals(4, heap.extraerMinimo());
    }

    @Test
    void extraerEnVacioLanzaExcepcion() {
        MinHeap<Integer> heap = new MinHeap<>();
        assertThrows(NoSuchElementException.class, heap::extraerMinimo);
    }

    @Test
    void rechazaNull() {
        MinHeap<Integer> heap = new MinHeap<>();
        assertThrows(IllegalArgumentException.class, () -> heap.insertar(null));
    }

    @Test
    void crecePasadoLaCapacidadInicial() {
        MinHeap<Integer> heap = new MinHeap<>();
        Random azar = new Random(7);
        for (int i = 0; i < 1000; i++) {
            heap.insertar(azar.nextInt(500));
        }
        int anterior = heap.extraerMinimo();
        for (int i = 1; i < 1000; i++) {
            int actual = heap.extraerMinimo();
            assertTrue(actual >= anterior);
            anterior = actual;
        }
    }
}