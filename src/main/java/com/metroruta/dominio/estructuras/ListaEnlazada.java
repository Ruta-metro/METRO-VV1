package com.metroruta.dominio.estructuras;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class ListaEnlazada<T> implements Iterable<T> {

    // cada nodo guarda un dato y apunta al siguiente
    private static class Nodo<T> {
        T dato;
        Nodo<T> siguiente;

        Nodo(T dato) {
            this.dato = dato;
        }
    }

    private Nodo<T> primero;
    private Nodo<T> ultimo; // asi agregar al final es O(1)
    private int tamanio;

    public void agregar(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (primero == null) {
            primero = nuevo;
        } else {
            ultimo.siguiente = nuevo;
        }
        ultimo = nuevo;
        tamanio++;
    }

    // borra los elementos que cumplan la condicion y devuelve cuantos borro
    public int eliminarSi(Predicate<T> condicion) {
        int borrados = 0;
        Nodo<T> anterior = null;
        Nodo<T> actual = primero;
        while (actual != null) {
            if (condicion.test(actual.dato)) {
                if (anterior == null) {
                    primero = actual.siguiente;
                } else {
                    anterior.siguiente = actual.siguiente;
                }
                if (actual == ultimo) {
                    ultimo = anterior;
                }
                tamanio--;
                borrados++;
            } else {
                anterior = actual;
            }
            actual = actual.siguiente;
        }
        return borrados;
    }

    public int tamanio() {
        return tamanio;
    }

    public boolean estaVacia() {
        return tamanio == 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private Nodo<T> actual = primero;

            @Override
            public boolean hasNext() {
                return actual != null;
            }

            @Override
            public T next() {
                if (actual == null) {
                    throw new NoSuchElementException();
                }
                T dato = actual.dato;
                actual = actual.siguiente;
                return dato;
            }
        };
    }
}
