package com.metroruta.structures;

import java.util.Arrays;
import java.util.NoSuchElementException;

public class MinHeap<T extends Comparable<T>> {

    // el heap vive en un arreglo: hijos de i en 2i+1 y 2i+2
    private Object[] datos = new Object[16];
    private int tamanio;

    public void insertar(T dato) {
        if (dato == null) {
            throw new IllegalArgumentException("No se puede insertar null");
        }
        if (tamanio == datos.length) {
            datos = Arrays.copyOf(datos, tamanio * 2); // duplica cuando se llena
        }
        datos[tamanio] = dato;
        subir(tamanio);
        tamanio++;
    }

    public T extraerMinimo() {
        if (estaVacio()) {
            throw new NoSuchElementException("El heap esta vacio");
        }
        T minimo = get(0);
        tamanio--;
        datos[0] = datos[tamanio]; // el ultimo pasa a la raiz
        datos[tamanio] = null;
        if (tamanio > 0) {
            bajar(0);
        }
        return minimo;
    }

    public boolean estaVacio() {
        return tamanio == 0;
    }

    public int tamanio() {
        return tamanio;
    }

    // sube el dato mientras sea menor que su padre
    private void subir(int i) {
        while (i > 0) {
            int padre = (i - 1) / 2;
            if (get(i).compareTo(get(padre)) >= 0) {
                break;
            }
            intercambiar(i, padre);
            i = padre;
        }
    }

    // baja el dato mientras algun hijo sea menor
    private void bajar(int i) {
        while (true) {
            int izq = 2 * i + 1;
            int der = 2 * i + 2;
            int menor = i;
            if (izq < tamanio && get(izq).compareTo(get(menor)) < 0) {
                menor = izq;
            }
            if (der < tamanio && get(der).compareTo(get(menor)) < 0) {
                menor = der;
            }
            if (menor == i) {
                break;
            }
            intercambiar(i, menor);
            i = menor;
        }
    }

    private void intercambiar(int a, int b) {
        Object aux = datos[a];
        datos[a] = datos[b];
        datos[b] = aux;
    }

    @SuppressWarnings("unchecked")
    private T get(int i) {
        return (T) datos[i];
    }
}