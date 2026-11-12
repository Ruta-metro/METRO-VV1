package com.metroruta.service;

import com.metroruta.dto.Ruta;
import com.metroruta.model.Conexion;
import com.metroruta.model.Estacion;
import com.metroruta.repository.ConexionRepository;
import com.metroruta.repository.EstacionRepository;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

// dibuja la red como un SVG: lineas, estaciones, la ruta resaltada y las estaciones cerradas
@Service
public class DiagramaService {

    private static final Logger log = LoggerFactory.getLogger(DiagramaService.class);

    public static final int ANCHO = 1180;
    public static final int ALTO = CartografiaMetro.TAMANO;

    // Coordenadas de la versión anterior, para migrar solo posiciones automáticas.
    private static final Map<String, int[]> POSICIONES_ANTERIORES = new HashMap<>();

    static {
        String[] lineaA = {"Niquía", "Bello", "Madera", "Acevedo", "Tricentenario", "Caribe", "Universidad",
            "Hospital", "Prado", "Parque Berrío", "San Antonio", "Alpujarra", "Exposiciones", "Industriales",
            "Poblado", "Aguacatala", "Ayurá", "Envigado", "Itagüí", "Sabaneta", "La Estrella"};
        for (int i = 0; i < lineaA.length; i++) {
            POSICIONES_ANTERIORES.put(lineaA[i], new int[]{40 + 50 * i, 360});
        }
        // Linea B baja hacia la izquierda desde San Antonio
        String[] lineaB = {"Cisneros", "Suramericana", "Estadio", "Floresta", "Santa Lucía", "San Javier"};
        for (int i = 0; i < lineaB.length; i++) {
            POSICIONES_ANTERIORES.put(lineaB[i], new int[]{540 - 55 * (i + 1), 360 + 50 * (i + 1)});
        }
        // el tranvia baja hacia la derecha
        String[] tranvia = {"San José", "Pabellón del Agua EPM", "Bicentenario", "Buenos Aires", "Miraflores",
            "Loyola", "Alejandro Echavarría", "Oriente"};
        for (int i = 0; i < tranvia.length; i++) {
            POSICIONES_ANTERIORES.put(tranvia[i], new int[]{540 + 55 * (i + 1), 360 + 35 * (i + 1)});
        }
        // el Metrocable K baja desde Acevedo
        String[] metrocable = {"Andalucía", "Popular", "Santo Domingo Savio"};
        for (int i = 0; i < metrocable.length; i++) {
            POSICIONES_ANTERIORES.put(metrocable[i], new int[]{190 - 25 * (i + 1), 360 + 60 * (i + 1)});
        }
    }

    private final EstacionRepository estacionRepository;
    private final ConexionRepository conexionRepository;

    public DiagramaService(EstacionRepository estacionRepository, ConexionRepository conexionRepository) {
        this.estacionRepository = estacionRepository;
        this.conexionRepository = conexionRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void alIniciar() {
        asegurarPosiciones();
    }

    // le pone posicion a las estaciones que no tienen; devuelve cuantas arreglo
    public int asegurarPosiciones() {
        int cambiadas = 0;
        for (Estacion estacion : estacionRepository.findAll()) {
            var geografia = CartografiaMetro.estacion(estacion.getNombre());
            int[] anterior = POSICIONES_ANTERIORES.get(estacion.getNombre());
            boolean vacia = estacion.getPosX() == null || estacion.getPosY() == null;
            boolean heredada = anterior != null && !vacia
                    && estacion.getPosX() == anterior[0] && estacion.getPosY() == anterior[1];
            if (!vacia && !heredada) {
                continue;
            }
            int[] posicion = geografia == null ? posicionLibre() : new int[]{geografia.x(), geografia.y()};
            estacion.setPosX(posicion[0]);
            estacion.setPosY(posicion[1]);
            estacionRepository.save(estacion);
            cambiadas++;
        }
        if (cambiadas > 0) {
            log.info("Posiciones del diagrama asignadas a {} estaciones", cambiadas);
        }
        return cambiadas;
    }

    // para una estacion nueva: una fila de abajo, de izquierda a derecha
    public int[] posicionLibre() {
        int abajo = 0;
        for (Estacion estacion : estacionRepository.findAll()) {
            if (estacion.getPosY() != null && estacion.getPosY() >= 850) {
                abajo++;
            }
        }
        return new int[]{40 + 110 * (abajo % 8), 850 + 36 * (abajo / 8)};
    }

    // ruta puede ser null: entonces solo se dibuja la red
    public record Mapa(String svg, Map<String, String> lineas) {}

    private static final Set<String> ESTACIONES_DE_REFERENCIA = Set.of(
            "Niquía", "Bello", "Acevedo", "Caribe", "Hospital", "San Antonio", "Cisneros", "Floresta",
            "San Javier", "Miraflores", "Oriente", "Industriales", "Poblado", "Envigado", "La Estrella",
            "La Palma", "Universidad de Medellín", "Parque Aranjuez");

    public String generarSvg(Ruta ruta) {
        return generarMapa(ruta).svg();
    }

    public Mapa generarMapa(Ruta ruta) {
        long inicio = System.nanoTime();
        List<Estacion> estaciones = estacionRepository.findAll();
        List<Conexion> conexiones = conexionRepository.findAll();

        Set<String> enRuta = new HashSet<>();
        Map<String, Integer> tramosRuta = new HashMap<>();
        Set<String> cambiosRuta = new HashSet<>();
        int etapa = 0;
        if (ruta != null) {
            List<Ruta.Paso> pasos = ruta.pasos();
            for (int i = 0; i < pasos.size(); i++) {
                enRuta.add(pasos.get(i).estacion());
                if (pasos.get(i).transbordo()) {
                    cambiosRuta.add(pasos.get(i).estacion());
                    etapa++;
                } else if (i > 0) {
                    tramosRuta.put(claveTramo(pasos.get(i - 1).estacion(), pasos.get(i).estacion(), pasos.get(i).linea()), etapa);
                }
            }
        }

        Map<String, Estacion> porNombre = new LinkedHashMap<>();
        for (Estacion estacion : estaciones) {
            if (estacion.getPosX() != null && estacion.getPosY() != null) {
                porNombre.put(estacion.getNombre(), estacion);
            }
        }
        Map<String, Set<String>> lineasPorEstacion = new HashMap<>();
        Map<String, Integer> gradoPorEstacion = new HashMap<>();
        Map<String, String> colores = new LinkedHashMap<>();
        for (Conexion c : conexiones) {
            lineasPorEstacion.computeIfAbsent(c.getOrigen().getNombre(), k -> new HashSet<>()).add(c.getLinea().getNombre());
            lineasPorEstacion.computeIfAbsent(c.getDestino().getNombre(), k -> new HashSet<>()).add(c.getLinea().getNombre());
            gradoPorEstacion.merge(c.getOrigen().getNombre(), 1, Integer::sum);
            gradoPorEstacion.merge(c.getDestino().getNombre(), 1, Integer::sum);
            colores.putIfAbsent(c.getLinea().getNombre(),
                    CartografiaMetro.color(c.getLinea().getNombre(), c.getLinea().getColor()));
        }

        int ancho = CartografiaMetro.TAMANO;
        int alto = CartografiaMetro.TAMANO;
        for (Estacion estacion : porNombre.values()) {
            ancho = Math.max(ancho, estacion.getPosX() + 24);
            alto = Math.max(alto, estacion.getPosY() + 24);
        }
        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 ").append(ancho).append(' ').append(alto)
                .append("\" style=\"--proporcion-mapa:").append((double) ancho / alto)
                .append("\" class=\"mapa-red\" role=\"img\" aria-label=\"Mapa geográfico de la red del Metro de Medellín\">")
                .append("<g class=\"fondo-cartografico\">").append(CartografiaMetro.fondo()).append("</g>");

        StringBuilder trazosBase = new StringBuilder();
        for (Conexion c : conexiones) {
            Estacion a = porNombre.get(c.getOrigen().getNombre());
            Estacion b = porNombre.get(c.getDestino().getNombre());
            if (a == null || b == null) {
                continue;
            }
            String nombre = c.getLinea().getNombre();
            String geometria = geometria(a, b, nombre);
            if (!tramosRuta.containsKey(claveTramo(a.getNombre(), b.getNombre(), nombre))) {
                svg.append(trazo(geometria, "#fffdf7", 5, "borde-tramo"));
                String clase = ruta == null ? "tramo" : "tramo atenuado";
                clase += switch (nombre) {
                    case "Buses Línea 2" -> " bus-linea-2";
                    case "Buses Línea O" -> " bus-linea-o";
                    default -> "";
                };
                trazosBase.append(trazo(geometria, colores.get(nombre), 3, clase));
            }
        }

        svg.append(trazosBase);

        for (Conexion c : conexiones) {
            Estacion a = porNombre.get(c.getOrigen().getNombre());
            Estacion b = porNombre.get(c.getDestino().getNombre());
            String nombre = c.getLinea().getNombre();
            if (a == null || b == null) {
                continue;
            }
            Integer tramo = tramosRuta.get(claveTramo(a.getNombre(), b.getNombre(), nombre));
            if (tramo != null) {
                boolean rosa = tramo % 2 == 0;
                String geometria = geometria(a, b, nombre);
                svg.append(trazo(geometria, rosa ? "#a62c67" : "#6f40ab", 8, "borde-ruta"));
                svg.append(trazo(geometria, rosa ? "#ffc1dc" : "#d8b9ff", 5, "tramo-ruta"));
            }
        }

        for (Estacion e : porNombre.values()) {
            boolean transbordo = lineasPorEstacion.getOrDefault(e.getNombre(), Set.of()).size() > 1;
            boolean usada = enRuta.contains(e.getNombre());
            boolean cambio = cambiosRuta.contains(e.getNombre());
            boolean extremo = ruta != null && (e.getNombre().equals(ruta.origen()) || e.getNombre().equals(ruta.destino()));
            int radio = cambio || extremo ? 5 : transbordo ? 4 : 3;
            if (e.isCerrada()) {
                radio = 5;
            }
            String relleno = e.isCerrada() ? "#d32f2f" : cambio ? "#d8b9ff" : usada ? "#fff1f8" : "#ffffff";
            // El cliente mantiene el marcador en píxeles aunque cambie la escala de la geografía.
            svg.append("<g class=\"marcador-mapa\" data-estacion=\"").append(escapar(e.getNombre()))
                    .append("\" data-x=\"").append(e.getPosX()).append("\" data-y=\"").append(e.getPosY())
                    .append("\" transform=\"translate(").append(e.getPosX()).append(' ').append(e.getPosY()).append(")\">")
                    .append("<title>").append(escapar(e.getNombre())).append(e.isCerrada() ? " · Cerrada" : "")
                    .append("</title><circle class=\"").append(e.isCerrada() ? "estacion-cerrada" : "estacion")
                    .append("\" cx=\"0\" cy=\"0\" r=\"").append(radio)
                    .append("\" fill=\"").append(relleno).append("\" stroke=\"").append(cambio ? "#a62c67" : usada ? "#6f40ab" : "#172d39")
                    .append("\" stroke-width=\"")
                    .append(cambio ? 2 : 1.4).append("\"/>");
            if (e.isCerrada()) {
                svg.append("<path d=\"M-2.5,-2.5 2.5,2.5 M2.5,-2.5 -2.5,2.5\" stroke=\"#fff\" stroke-width=\"1.6\"/>");
            }
            svg.append("</g>");
        }

        for (Estacion e : porNombre.values()) {
            String nombre = e.getNombre();
            boolean usada = enRuta.contains(nombre);
            boolean principal = cambiosRuta.contains(nombre)
                    || (ruta != null && (nombre.equals(ruta.origen()) || nombre.equals(ruta.destino())));
            boolean referencia = ESTACIONES_DE_REFERENCIA.contains(nombre)
                    || gradoPorEstacion.getOrDefault(nombre, 0) == 1;
            svg.append(etiqueta(e, usada, principal ? 0 : e.isCerrada() ? 1 : referencia ? 2 : usada ? 3 : 4));
        }

        svg.append("</svg>");

        log.info("Diagrama generado: {} estaciones, {} tramos en {} us", porNombre.size(), conexiones.size(),
                (System.nanoTime() - inicio) / 1000);
        return new Mapa(svg.toString(), java.util.Collections.unmodifiableMap(colores));
    }

    private String geometria(Estacion a, Estacion b, String linea) {
        var vertices = esPosicionCartografica(a) && esPosicionCartografica(b)
                ? CartografiaMetro.vertices(linea, a.getNombre(), b.getNombre()) : List.<String>of();
        String medio = vertices.isEmpty() ? "" : String.join(" ", vertices) + " ";
        return "M" + a.getPosX() + "," + a.getPosY() + " " + medio + b.getPosX() + "," + b.getPosY();
    }

    private boolean esPosicionCartografica(Estacion estacion) {
        var ubicacion = CartografiaMetro.estacion(estacion.getNombre());
        return ubicacion != null && estacion.getPosX() == ubicacion.x() && estacion.getPosY() == ubicacion.y();
    }

    private String trazo(String geometria, String color, int grosor, String clase) {
        return "<path class=\"" + clase + "\" d=\"" + geometria + "\" fill=\"none\" stroke=\""
                + escapar(color) + "\" stroke-width=\"" + grosor + "\" vector-effect=\"non-scaling-stroke\" stroke-linecap=\"round\" stroke-linejoin=\"round\"/>";
    }

    private String etiqueta(Estacion estacion, boolean usada, int prioridad) {
        int x = estacion.getPosX();
        int y = estacion.getPosY();
        var ubicacion = CartografiaMetro.estacion(estacion.getNombre());
        String lado = ubicacion != null ? ubicacion.ancla() : "start";
        String grupo = "<g class=\"etiqueta-mapa\" data-prioridad=\"" + prioridad
                + "\" data-x=\"" + x + "\" data-y=\"" + y + "\" data-lado=\"" + lado + "\">";
        return grupo + "<text class=\"etiqueta-estacion\" font-weight=\"" + (usada ? 700 : 500)
                + "\">" + escapar(estacion.getNombre()) + "</text></g>";
    }

    // el mismo tramo se escribe igual sin importar el sentido
    private static String claveTramo(String a, String b, String linea) {
        return (a.compareTo(b) < 0 ? a + "|" + b : b + "|" + a) + "|" + linea;
    }

    static String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
