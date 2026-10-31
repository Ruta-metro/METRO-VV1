package com.metroruta.structures;

import java.util.NoSuchElementException;

public class Pila<T> {

    // cada nodo apunta al que esta debajo de el
    private static class Nodo<T> {
        T dato;
        Nodo<T> abajo;

        Nodo(T dato, Nodo<T> abajo) {
            this.dato = dato;
            this.abajo = abajo;
        }
    }

    private Nodo<T> tope;
    private int tamanio;

    public void apilar(T dato) {
        tope = new Nodo<>(dato, tope);
        tamanio++;
    }

    public T desapilar() {
        if (estaVacia()) {
            throw new NoSuchElementException("La pila esta vacia");
        }
        T dato = tope.dato;
        tope = tope.abajo;
        tamanio--;
        return dato;
    }

    // mira el tope sin sacarlo
    public T cima() {
        if (estaVacia()) {
            throw new NoSuchElementException("La pila esta vacia");
        }
        return tope.dato;
    }

    public boolean estaVacia() {
        return tope == null;
    }

    public int tamanio() {
        return tamanio;
    }
}