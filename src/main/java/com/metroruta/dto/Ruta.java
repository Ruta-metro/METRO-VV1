package com.metroruta.dto;

import com.metroruta.model.CriterioRuta;
import java.util.List;

// una ruta ya calculada: los pasos en orden, el tiempo total y los transbordos
public record Ruta(String origen, String destino, CriterioRuta criterio,
                   List<Paso> pasos, int minutos, int transbordos) {

    // un paso es estar en una estacion de una linea; si llego por transbordo, transbordo es true
    public record Paso(String estacion, String linea, int minutosAcumulados, boolean transbordo) {
    }
}
