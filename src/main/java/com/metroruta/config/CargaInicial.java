package com.metroruta.config;

import com.metroruta.model.Conexion;
import com.metroruta.model.Estacion;
import com.metroruta.model.Linea;
import com.metroruta.repository.ConexionRepository;
import com.metroruta.repository.EstacionRepository;
import com.metroruta.repository.LineaRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// carga la red del Metro la primera vez que arranca la app (si la base de datos esta vacia)
@Component
@Transactional
public class CargaInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CargaInicial.class);

    // una linea: sus estaciones en orden y los minutos de cada tramo (hay un tramo menos que estaciones)
    private record DatosLinea(String nombre, String color, String[] estaciones, int[] minutos) {
    }

    // los minutos son estimados, ver README.md
    private static final DatosLinea[] RED = {
        new DatosLinea("Línea A", "#2E9E4F", new String[]{
            "Niquía", "Bello", "Madera", "Acevedo", "Tricentenario", "Caribe", "Universidad",
            "Hospital", "Prado", "Parque Berrío", "San Antonio", "Alpujarra", "Exposiciones",
            "Industriales", "Poblado", "Aguacatala", "Ayurá", "Envigado", "Itagüí", "Sabaneta",
            "La Estrella"}, iguales(2, 20)),
        new DatosLinea("Línea B", "#F28C28", new String[]{
            "San Antonio", "Cisneros", "Suramericana", "Estadio", "Floresta", "Santa Lucía",
            "San Javier"}, iguales(2, 6)),
        new DatosLinea("Tranvía de Ayacucho", "#C8283C", new String[]{
            "San Antonio", "San José", "Pabellón del Agua EPM", "Bicentenario", "Buenos Aires",
            "Miraflores", "Loyola", "Alejandro Echavarría", "Oriente"}, iguales(2, 8)),
        new DatosLinea("Metrocable Línea K", "#E0B400", new String[]{
            "Acevedo", "Andalucía", "Popular", "Santo Domingo Savio"}, new int[]{3, 3, 4}),
    };

    @Value("${metroruta.red.ampliada:true}")
    private boolean ampliarRed;

    private final LineaRepository lineaRepository;
    private final EstacionRepository estacionRepository;
    private final ConexionRepository conexionRepository;

    public CargaInicial(LineaRepository lineaRepository, EstacionRepository estacionRepository,
                        ConexionRepository conexionRepository) {
        this.lineaRepository = lineaRepository;
        this.estacionRepository = estacionRepository;
        this.conexionRepository = conexionRepository;
    }

    private static int[] iguales(int minutos, int cantidad) {
        int[] tramos = new int[cantidad];
        Arrays.fill(tramos, minutos);
        return tramos;
    }

    @Override
    public void run(ApplicationArguments args) {
        cargarSiVacia();
        if (ampliarRed) {
            cargarLineasAdicionales();
        }
    }

    private void cargarLineasAdicionales() {
        var recurso = CargaInicial.class.getResourceAsStream("/mapa/lineas-adicionales.txt");
        if (recurso == null) {
            throw new IllegalStateException("Falta el inventario de líneas adicionales");
        }
        Map<String, Estacion> estaciones = new HashMap<>();
        estacionRepository.findAll().forEach(estacion -> estaciones.put(estacion.getNombre(), estacion));
        try (var lector = new BufferedReader(new InputStreamReader(recurso, StandardCharsets.UTF_8))) {
            for (String fila; (fila = lector.readLine()) != null;) {
                String[] campos = fila.split("\\|");
                if (lineaRepository.findByNombre(campos[0]).isPresent()) {
                    continue;
                }
                int[] minutos = Arrays.stream(campos[2].split(",")).mapToInt(Integer::parseInt).toArray();
                cargarLinea(new DatosLinea(campos[0], campos[1], Arrays.copyOfRange(campos, 3, campos.length), minutos),
                        estaciones);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void cargarLinea(DatosLinea datos, Map<String, Estacion> estaciones) {
        if (datos.minutos().length != datos.estaciones().length - 1) {
            throw new IllegalStateException("Los minutos de " + datos.nombre() + " no cuadran con sus estaciones");
        }
        Linea linea = lineaRepository.save(new Linea(datos.nombre(), datos.color()));
        for (String nombre : datos.estaciones()) {
            estaciones.computeIfAbsent(nombre, clave -> estacionRepository.save(new Estacion(clave)));
        }
        for (int i = 0; i < datos.minutos().length; i++) {
            Estacion origen = estaciones.get(datos.estaciones()[i]);
            Estacion destino = estaciones.get(datos.estaciones()[i + 1]);
            conexionRepository.save(new Conexion(origen, destino, linea, datos.minutos()[i]));
        }
    }

    // devuelve true si cargo la red y false si ya estaba cargada
    public boolean cargarSiVacia() {
        long existentes = estacionRepository.count();
        if (existentes > 0) {
            log.info("La red ya esta cargada ({} estaciones), no se vuelve a cargar", existentes);
            return false;
        }

        long inicio = System.nanoTime();
        Map<String, Estacion> estaciones = new HashMap<>();
        int totalConexiones = 0;

        for (DatosLinea datos : RED) {
            cargarLinea(datos, estaciones);
            totalConexiones += datos.minutos().length;
        }

        long ms = (System.nanoTime() - inicio) / 1_000_000;
        log.info("Red cargada: {} lineas, {} estaciones, {} conexiones en {} ms",
                RED.length, estaciones.size(), totalConexiones, ms);
        return true;
    }
}
