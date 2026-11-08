package com.metroruta.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DifusorEventosTest {

    @Test
    void todosLosSuscritosRecibenElMensaje() {
        DifusorEventos difusor = new DifusorEventos();
        List<String> a = new ArrayList<>();
        List<String> b = new ArrayList<>();
        difusor.suscribir(a::add);
        difusor.suscribir(b::add);
        difusor.emitir("hola");
        assertEquals(List.of("hola"), a);
        assertEquals(List.of("hola"), b);
    }

    @Test
    void quienCancelaYaNoRecibeNada() {
        DifusorEventos difusor = new DifusorEventos();
        List<String> recibidos = new ArrayList<>();
        Runnable cancelar = difusor.suscribir(recibidos::add);
        cancelar.run();
        difusor.emitir("hola");
        assertEquals(0, recibidos.size());
        assertEquals(0, difusor.cantidadOyentes());
    }

    @Test
    void unaSesionQueFallaNoImpideAvisarALasDemas() {
        DifusorEventos difusor = new DifusorEventos();
        List<String> recibidos = new ArrayList<>();
        difusor.suscribir(m -> {
            throw new IllegalStateException("sesion caida");
        });
        difusor.suscribir(recibidos::add);
        difusor.emitir("hola");
        assertEquals(List.of("hola"), recibidos);
    }

    @Test
    void sinSuscritosNoPasaNada() {
        new DifusorEventos().emitir("nadie escucha");
    }
}
