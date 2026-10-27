package com.metroruta.dominio.estructuras;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class TrieTest {

    private Trie trieDePrueba() {
        Trie trie = new Trie();
        trie.insertar("San Antonio");
        trie.insertar("San Javier");
        trie.insertar("Sabaneta");
        trie.insertar("Itagüí");
        return trie;
    }

    @Test
    void buscaPorPrefijo() {
        Trie trie = trieDePrueba();
        assertEquals(List.of("San Antonio", "San Javier"), trie.buscarPorPrefijo("san"));
        assertEquals(List.of("Sabaneta"), trie.buscarPorPrefijo("saba"));
    }

    @Test
    void ignoraMayusculasYTildes() {
        Trie trie = trieDePrueba();
        assertEquals(List.of("Itagüí"), trie.buscarPorPrefijo("ITAGUI"));
        assertEquals(List.of("Itagüí"), trie.buscarPorPrefijo("itag"));
    }

    @Test
    void prefijoQueNoExisteDevuelveVacio() {
        Trie trie = trieDePrueba();
        assertTrue(trie.buscarPorPrefijo("zzz").isEmpty());
    }

    @Test
    void prefijoVacioDevuelveTodas() {
        Trie trie = trieDePrueba();
        assertEquals(4, trie.buscarPorPrefijo("").size());
    }

    @Test
    void eliminaSinDanarLasQueComparten() {
        Trie trie = new Trie();
        trie.insertar("San");
        trie.insertar("San Antonio");
        assertTrue(trie.eliminar("San"));
        assertEquals(List.of("San Antonio"), trie.buscarPorPrefijo("san"));
    }

    @Test
    void eliminaYLimpiaLaRama() {
        Trie trie = trieDePrueba();
        assertTrue(trie.eliminar("sabaneta"));
        assertTrue(trie.buscarPorPrefijo("saba").isEmpty());
    }

    @Test
    void eliminarInexistenteDevuelveFalse() {
        Trie trie = trieDePrueba();
        assertFalse(trie.eliminar("Poblado"));
        assertFalse(trie.eliminar("San")); // es solo prefijo, no palabra
    }

    @Test
    void rechazaPalabrasVacias() {
        Trie trie = new Trie();
        assertThrows(IllegalArgumentException.class, () -> trie.insertar(""));
        assertThrows(IllegalArgumentException.class, () -> trie.insertar("   "));
    }
}