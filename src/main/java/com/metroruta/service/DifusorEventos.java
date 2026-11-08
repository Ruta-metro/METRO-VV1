package com.metroruta.service;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// avisa a todas las sesiones abiertas cuando cambia la red (cada vista se suscribe)
@Component
public class DifusorEventos {

    private static final Logger log = LoggerFactory.getLogger(DifusorEventos.class);

    // lista segura para recorrerla mientras otra sesion se suscribe o se va
    private final CopyOnWriteArrayList<Consumer<String>> oyentes = new CopyOnWriteArrayList<>();

    // devuelve lo que hay que ejecutar para cancelar la suscripcion
    public Runnable suscribir(Consumer<String> oyente) {
        oyentes.add(oyente);
        return () -> oyentes.remove(oyente);
    }

    public void emitir(String mensaje) {
        int enviados = 0;
        for (Consumer<String> oyente : oyentes) {
            try {
                oyente.accept(mensaje);
                enviados++;
            } catch (RuntimeException e) {
                // una sesion caida no debe impedir avisar a las demas
                log.warn("No se pudo avisar a una sesion: {}", e.getMessage());
            }
        }
        log.info("Evento '{}' enviado a {} sesiones", mensaje, enviados);
    }

    public int cantidadOyentes() {
        return oyentes.size();
    }
}
