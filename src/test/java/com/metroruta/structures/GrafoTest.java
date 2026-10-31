package com.metroruta.structures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GrafoTest {

    private int contar(Iterable<?> cosas) {
        int n = 0;
        for (Object o : cosas) {
            n++;
        }
        return n;
    }

    // grafo chico: A - B - C y A - C
    private Grafo grafoDePrueba() {
        Grafo g = new Grafo();
        g.agregarNodo("A");
        g.agregarNodo("B");
        g.agregarNodo("C");
        g.conectar("A", "B", 2, false);
        g.conectar("B", "C", 3, false);
        g.conectar("A", "C", 5, true);
        return g;
    }

    @Test
    void conectaEnLosDosSentidos() {
        Grafo g = grafoDePrueba();
        assertEquals(2, contar(g.vecinos("A")));
        assertEquals(2, contar(g.vecinos("B")));
        assertEquals(2, contar(g.vecinos("C")));
    }

    @Test
    void guardaMinutosYTransbordo() {
        Grafo g = grafoDePrueba();
        boolean encontrado = false;
        for (Grafo.Arista a : g.vecinos("A")) {
            if (a.destino().equals("C")) {
                encontrado = true;
                assertEquals(5, a.minutos());
                assertTrue(a.transbordo());
            }
        }
        assertTrue(encontrado);
    }

    @Test
    void rechazaMinutosNoPositivos() {
        Grafo g = grafoDePrueba();
        assertThrows(IllegalArgumentException.class, () -> g.conectar("A", "B", 0, false));
        assertThrows(IllegalArgumentException.class, () -> g.conectar("A", "B", -1, false));
    }

    @Test
    void rechazaNodosQueNoExisten() {
        Grafo g = grafoDePrueba();
        assertThrows(IllegalArgumentException.class, () -> g.conectar("A", "Z", 2, false));
        assertThrows(IllegalArgumentException.class, () -> g.vecinos("Z"));
        assertThrows(IllegalArgumentException.class, () -> g.cerrar("Z"));
    }

    @Test
    void cierraYAbreNodos() {
        Grafo g = grafoDePrueba();
        assertFalse(g.estaCerrado("B"));
        g.cerrar("B");
        assertTrue(g.estaCerrado("B"));
        g.abrir("B");
        assertFalse(g.estaCerrado("B"));
    }

    @Test
    void desconectaEnLosDosSentidos() {
        Grafo g = grafoDePrueba();
        g.desconectar("A", "B");
        assertEquals(1, contar(g.vecinos("A")));
        assertEquals(1, contar(g.vecinos("B")));
    }

    @Test
    void eliminarNodoQuitaSusConexiones() {
        Grafo g = grafoDePrueba();
        g.eliminarNodo("B");
        assertFalse(g.existeNodo("B"));
        assertEquals(2, g.cantidadNodos());
        assertEquals(1, contar(g.vecinos("A")));
        assertEquals(1, contar(g.vecinos("C")));
    }
}
