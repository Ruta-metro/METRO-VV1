package com.metroruta.dto;

import java.util.List;

// un tramo de la ruta que se hace seguido en una sola linea
public record Segmento(String linea, List<String> estaciones, int minutos, int minutosTransbordoPrevio) {
}
