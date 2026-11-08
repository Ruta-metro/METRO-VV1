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
    public static final int ALTO = 760;

    // posiciones de las estaciones del Metro (x, y); las nuevas se ubican abajo
    private static final Map<String, int[]> POSICIONES = new HashMap<>();

    static {
        String[] lineaA = {"Niquía", "Bello", "Madera", "Acevedo", "Tricentenario", "Caribe", "Universidad",
            "Hospital", "Prado", "Parque Berrío", "San Antonio", "Alpujarra", "Exposiciones", "Industriales",
            "Poblado", "Aguacatala", "Ayurá", "Envigado", "Itagüí", "Sabaneta", "La Estrella"};
        for (int i = 0; i < lineaA.length; i++) {
            POSICIONES.put(lineaA[i], new int[]{40 + 50 * i, 360});
        }
        // Linea B baja hacia la izquierda desde San Antonio
        String[] lineaB = {"Cisneros", "Suramericana", "Estadio", "Floresta", "Santa Lucía", "San Javier"};
        for (int i = 0; i < lineaB.length; i++) {
            POSICIONES.put(lineaB[i], new int[]{540 - 55 * (i + 1), 360 + 50 * (i + 1)});
        }
        // el tranvia baja hacia la derecha
        String[] tranvia = {"San José", "Pabellón del Agua EPM", "Bicentenario", "Buenos Aires", "Miraflores",
            "Loyola", "Alejandro Echavarría", "Oriente"};
        for (int i = 0; i < tranvia.length; i++) {
            POSICIONES.put(tranvia[i], new int[]{540 + 55 * (i + 1), 360 + 35 * (i + 1)});
        }
        // el Metrocable K baja desde Acevedo
        String[] metrocable = {"Andalucía", "Popular", "Santo Domingo Savio"};
        for (int i = 0; i < metrocable.length; i++) {
            POSICIONES.put(metrocable[i], new int[]{190 - 25 * (i + 1), 360 + 60 * (i + 1)});
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
            if (estacion.getPosX() != null && estacion.getPosY() != null) {
                continue;
            }
            int[] posicion = POSICIONES.get(estacion.getNombre());
            if (posicion == null) {
                posicion = posicionLibre();
            }
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
            if (estacion.getPosY() != null && estacion.getPosY() >= 700) {
                abajo++;
            }
        }
        return new int[]{40 + 80 * (abajo % 14), 700 + 40 * (abajo / 14)};
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
            colores.putIfAbsent(c.getLinea().getNombre(), c.getLinea().getColor());
        }

        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 ").append(ANCHO).append(' ').append(ALTO)
                .append("\" width=\"100%\" style=\"max-width:").append(ANCHO)
                .append("px;font-family:sans-serif\" role=\"img\" aria-label=\"Diagrama de la red del Metro\">");

        // primero el resaltado, para que quede debajo de las lineas
        for (Conexion c : conexiones) {
            Estacion a = porNombre.get(c.getOrigen().getNombre());
            Estacion b = porNombre.get(c.getDestino().getNombre());
            if (a != null && b != null && tramosRuta.contains(claveTramo(a.getNombre(), b.getNombre(), c.getLinea().getNombre()))) {
                svg.append(linea(a, b, "#111111", 16, "tramo-ruta"));
            }
        }
        for (Conexion c : conexiones) {
            Estacion a = porNombre.get(c.getOrigen().getNombre());
            Estacion b = porNombre.get(c.getDestino().getNombre());
            if (a != null && b != null) {
                svg.append(linea(a, b, c.getLinea().getColor(), 8, "tramo"));
            }
        }

        for (Estacion e : porNombre.values()) {
            boolean transbordo = lineasPorEstacion.getOrDefault(e.getNombre(), Set.of()).size() > 1;
            boolean usada = enRuta.contains(e.getNombre());
            int radio = transbordo ? 10 : 7;
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
            svg.append(etiqueta(e, lineasPorEstacion.getOrDefault(e.getNombre(), Set.of()), enRuta.contains(e.getNombre())));
        }

        // leyenda con las lineas y los simbolos
        int y = 24;
        for (Map.Entry<String, String> linea : colores.entrySet()) {
            svg.append("<rect x=\"20\" y=\"").append(y - 10).append("\" width=\"26\" height=\"8\" fill=\"")
                    .append(escapar(linea.getValue())).append("\"/><text x=\"54\" y=\"").append(y - 2)
                    .append("\" font-size=\"13\" fill=\"#222222\">").append(escapar(linea.getKey())).append("</text>");
            y += 20;
        }
        svg.append("<circle cx=\"33\" cy=\"").append(y - 6).append("\" r=\"7\" fill=\"#d32f2f\" stroke=\"#111111\" stroke-width=\"2\"/>")
                .append("<text x=\"54\" y=\"").append(y - 2).append("\" font-size=\"13\" fill=\"#222222\">Estación cerrada</text>");
        svg.append("</svg>");

        log.info("Diagrama generado: {} estaciones, {} tramos en {} us", porNombre.size(), conexiones.size(),
                (System.nanoTime() - inicio) / 1000);
        return svg.toString();
    }

    private String linea(Estacion a, Estacion b, String color, int grosor, String clase) {
        return "<line class=\"" + clase + "\" x1=\"" + a.getPosX() + "\" y1=\"" + a.getPosY() + "\" x2=\"" + b.getPosX()
                + "\" y2=\"" + b.getPosY() + "\" stroke=\"" + escapar(color) + "\" stroke-width=\"" + grosor
                + "\" stroke-linecap=\"round\"/>";
    }

    // la linea A lleva el nombre arriba y en diagonal; las demas al lado
    private String etiqueta(Estacion e, Set<String> lineas, boolean usada) {
        String estilo = "font-size=\"12\" fill=\"#222222\"" + (usada ? " font-weight=\"bold\"" : "");
        boolean enLineaA = e.getPosY() == 360;
        String texto = escapar(e.getNombre());
        if (enLineaA) {
            return "<text transform=\"translate(" + (e.getPosX() + 4) + "," + (e.getPosY() - 16) + ") rotate(-45)\" "
                    + estilo + ">" + texto + "</text>";
        }
        boolean enTranvia = e.getPosX() > 540 && e.getPosY() > 360 && e.getPosY() < 700;
        if (enTranvia) {
            // el tranvia baja hacia la derecha, el nombre va abajo a la izquierda para no tapar la linea
            return "<text x=\"" + (e.getPosX() - 10) + "\" y=\"" + (e.getPosY() + 22) + "\" text-anchor=\"end\" " + estilo
                    + ">" + texto + "</text>";
        }
        boolean enLineaB = e.getPosX() >= 200 && e.getPosX() < 540 && e.getPosY() > 360 && e.getPosY() < 700;
        if (enLineaB) {
            return "<text x=\"" + (e.getPosX() - 14) + "\" y=\"" + (e.getPosY() + 4) + "\" text-anchor=\"end\" " + estilo + ">"
                    + texto + "</text>";
        }
        return "<text x=\"" + (e.getPosX() + 14) + "\" y=\"" + (e.getPosY() + 4) + "\" " + estilo + ">" + texto + "</text>";
    }

    // el mismo tramo se escribe igual sin importar el sentido
    private static String claveTramo(String a, String b, String linea) {
        return (a.compareTo(b) < 0 ? a + "|" + b : b + "|" + a) + "|" + linea;
    }

    static String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
