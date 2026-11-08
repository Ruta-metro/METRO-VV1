package com.metroruta.service;

import com.metroruta.dto.Ruta;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

// hora estimada de llegada a partir de la hora de salida
@Service
public class LlegadaService {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    public LocalDateTime ahora() {
        return LocalDateTime.now(ZONA);
    }

    public LocalDateTime horaLlegada(LocalDateTime salida, Ruta ruta) {
        if (salida == null || ruta == null) {
            throw new IllegalArgumentException("Falta la hora de salida o la ruta");
        }
        return salida.plusMinutes(ruta.minutos());
    }

    // si llega despues de medianoche lo aclara
    public String formatear(LocalDateTime salida, LocalDateTime llegada) {
        String hora = llegada.format(HORA);
        if (llegada.toLocalDate().isAfter(salida.toLocalDate())) {
            return hora + " (día siguiente)";
        }
        return hora;
    }
}
