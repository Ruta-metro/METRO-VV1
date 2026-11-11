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
    public String generarSvg(Ruta ruta) {
        long inicio = System.nanoTime();
        List<Estacion> estaciones = estacionRepository.findAll();
        List<Conexion> conexiones = conexionRepository.findAll();

        Set<String> enRuta = new HashSet<>();
        Set<String> tramosRuta = new HashSet<>();
        if (ruta != null) {
            List<Ruta.Paso> pasos = ruta.pasos();
            for (int i = 0; i < pasos.size(); i++) {
                enRuta.add(pasos.get(i).estacion());
                if (i > 0 && !pasos.get(i).transbordo()) {
                    tramosRuta.add(claveTramo(pasos.get(i - 1).estacion(), pasos.get(i).estacion(), pasos.get(i).linea()));
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
        Map<String, String> colores = new LinkedHashMap<>();
        for (Conexion c : conexiones) {
            lineasPorEstacion.computeIfAbsent(c.getOrigen().getNombre(), k -> new HashSet<>()).add(c.getLinea().getNombre());
            lineasPorEstacion.computeIfAbsent(c.getDestino().getNombre(), k -> new HashSet<>()).add(c.getLinea().getNombre());
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
                .append("\" width=\"100%\" class=\"mapa-red\" role=\"img\" aria-label=\"Mapa geográfico de la red del Metro de Medellín\">")
                .append(CartografiaMetro.fondo());

        for (Conexion c : conexiones) {
            Estacion a = porNombre.get(c.getOrigen().getNombre());
            Estacion b = porNombre.get(c.getDestino().getNombre());
            if (a == null || b == null) {
                continue;
            }
            String nombre = c.getLinea().getNombre();
            String geometria = geometria(a, b, nombre);
            boolean resaltado = tramosRuta.contains(claveTramo(a.getNombre(), b.getNombre(), nombre));
            svg.append(trazo(geometria, resaltado ? "#172d39" : "#fffdf7", resaltado ? 13 : 9,
                    resaltado ? "tramo-ruta" : "borde-tramo"));
            svg.append(trazo(geometria, colores.get(nombre), resaltado ? 7 : 5,
                    ruta != null && !resaltado ? "tramo atenuado" : "tramo"));
        }

        for (Estacion e : porNombre.values()) {
            boolean transbordo = lineasPorEstacion.getOrDefault(e.getNombre(), Set.of()).size() > 1;
            boolean usada = enRuta.contains(e.getNombre());
            int radio = transbordo ? 7 : 4;
            if (e.isCerrada()) {
                radio = 7;
            }
            String relleno = e.isCerrada() ? "#d32f2f" : usada ? "#111111" : "#ffffff";
            svg.append("<circle class=\"").append(e.isCerrada() ? "estacion-cerrada" : "estacion").append("\" cx=\"")
                    .append(e.getPosX()).append("\" cy=\"").append(e.getPosY()).append("\" r=\"").append(radio)
                    .append("\" fill=\"").append(relleno).append("\" stroke=\"#111111\" stroke-width=\"")
                    .append(transbordo ? 3 : 2).append("\"/>");
            if (e.isCerrada()) {
                // una equis blanca sobre el circulo rojo
                int d = 4;
                svg.append("<path d=\"M").append(e.getPosX() - d).append(' ').append(e.getPosY() - d).append('L')
                        .append(e.getPosX() + d).append(' ').append(e.getPosY() + d).append('M')
                        .append(e.getPosX() + d).append(' ').append(e.getPosY() - d).append('L')
                        .append(e.getPosX() - d).append(' ').append(e.getPosY() + d)
                        .append("\" stroke=\"#ffffff\" stroke-width=\"2\"/>");
            }
        }

        for (Estacion e : porNombre.values()) {
            svg.append(etiqueta(e, enRuta.contains(e.getNombre())));
        }

        svg.append(leyenda(colores));
        svg.append("</svg>");

        log.info("Diagrama generado: {} estaciones, {} tramos en {} us", porNombre.size(), conexiones.size(),
                (System.nanoTime() - inicio) / 1000);
        return svg.toString();
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
                + escapar(color) + "\" stroke-width=\"" + grosor + "\" stroke-linecap=\"round\" stroke-linejoin=\"round\"/>";
    }

    private String etiqueta(Estacion estacion, boolean usada) {
        int x = estacion.getPosX();
        int y = estacion.getPosY();
        var ubicacion = CartografiaMetro.estacion(estacion.getNombre());
        boolean cartografica = esPosicionCartografica(estacion);
        boolean aLaIzquierda = x > CartografiaMetro.TAMANO - 140;
        int textoX = cartografica ? ubicacion.textoX() : x + (aLaIzquierda ? -12 : 12);
        int textoY = cartografica ? ubicacion.textoY() : y + 4;
        String ancla = cartografica ? ubicacion.ancla() : (aLaIzquierda ? "end" : "start");
        String guia = "";
        if (Math.abs(textoX - x) > 18 || Math.abs(textoY - y - 4) > 8) {
            int finalX = textoX + ("end".equals(ancla) ? 4 : -4);
            guia = "<path d=\"M" + x + "," + y + " " + finalX + "," + (textoY - 4)
                    + "\" stroke=\"#84978c\" stroke-width=\"1\" fill=\"none\"/>";
        }
        return guia + "<text class=\"etiqueta-estacion\" x=\"" + textoX + "\" y=\"" + textoY
                + "\" text-anchor=\"" + ancla + "\" font-weight=\"" + (usada ? 800 : 600)
                + "\">" + escapar(estacion.getNombre()) + "</text>";
    }

    private String leyenda(Map<String, String> colores) {
        StringBuilder svg = new StringBuilder("<g class=\"leyenda-mapa\" transform=\"translate(24 24)\">");
        svg.append("<rect width=\"223\" height=\"").append(46 + colores.size() * 25)
                .append("\" rx=\"13\" fill=\"#fffdf7\" stroke=\"#dbd8cd\"/>");
        int y = 26;
        for (var linea : colores.entrySet()) {
            svg.append("<path d=\"M15 ").append(y - 4).append("h23\" stroke-width=\"5\" stroke-linecap=\"round\" stroke=\"")
                    .append(escapar(linea.getValue())).append("\"/><text x=\"48\" y=\"").append(y)
                    .append("\">").append(escapar(linea.getKey())).append("</text>");
            y += 25;
        }
        return svg.append("<circle cx=\"26\" cy=\"").append(y - 5)
                .append("\" r=\"5\" fill=\"#d32f2f\"/><text x=\"48\" y=\"").append(y)
                .append("\">Estación cerrada</text></g>").toString();
    }

    // el mismo tramo se escribe igual sin importar el sentido
    private static String claveTramo(String a, String b, String linea) {
        return (a.compareTo(b) < 0 ? a + "|" + b : b + "|" + a) + "|" + linea;
    }

    static String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
