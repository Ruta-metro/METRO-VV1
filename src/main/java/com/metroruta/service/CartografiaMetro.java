package com.metroruta.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class CartografiaMetro {
    static final int TAMANO = 900;

    record EstacionMapa(int x, int y, int textoX, int textoY, String ancla) {}
    private record Tramo(String linea, String origen, String destino) {}

    private static final Map<String, EstacionMapa> ESTACIONES = leerEstaciones();
    private static final Map<Tramo, List<String>> TRAMOS = leerTramos();
    private static final String FONDO = String.join("\n", leer("valle.svg"));

    private CartografiaMetro() {}

    static String fondo() {
        return FONDO;
    }

    static EstacionMapa estacion(String nombre) {
        return ESTACIONES.get(nombre);
    }

    static List<String> vertices(String linea, String origen, String destino) {
        var directo = TRAMOS.get(new Tramo(linea, origen, destino));
        if (directo != null) {
            return directo;
        }
        var inverso = TRAMOS.get(new Tramo(linea, destino, origen));
        return inverso == null ? List.of() : inverso.reversed();
    }

    static String color(String linea, String colorPorDefecto) {
        return switch (linea) {
            case "Línea A" -> "#1769a8";
            case "Línea B" -> "#cc620f";
            case "Tranvía de Ayacucho" -> "#147254";
            case "Metrocable Línea K" -> "#72912e";
            default -> colorPorDefecto;
        };
    }

    private static Map<String, EstacionMapa> leerEstaciones() {
        Map<String, EstacionMapa> estaciones = new HashMap<>();
        for (String fila : leer("estaciones.txt")) {
            String[] campo = fila.split("\\|");
            estaciones.put(campo[0], new EstacionMapa(Integer.parseInt(campo[1]), Integer.parseInt(campo[2]),
                    Integer.parseInt(campo[3]), Integer.parseInt(campo[4]), campo[5]));
        }
        return Map.copyOf(estaciones);
    }

    private static Map<Tramo, List<String>> leerTramos() {
        Map<Tramo, List<String>> tramos = new HashMap<>();
        for (String fila : leer("tramos.txt")) {
            String[] campo = fila.split("\\|");
            tramos.put(new Tramo(campo[0], campo[1], campo[2]), List.of(campo[3].split(" ")));
        }
        return Map.copyOf(tramos);
    }

    private static List<String> leer(String archivo) {
        var datos = CartografiaMetro.class.getResourceAsStream("/mapa/" + archivo);
        if (datos == null) {
            throw new IllegalStateException("Falta el recurso de cartografía " + archivo);
        }
        try (var lector = new BufferedReader(new InputStreamReader(datos, StandardCharsets.UTF_8))) {
            return lector.lines().toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
