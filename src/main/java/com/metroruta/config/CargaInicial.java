package com.metroruta.config;

import com.metroruta.model.Conexion;
import com.metroruta.model.Estacion;
import com.metroruta.model.Linea;
import com.metroruta.repository.ConexionRepository;
import com.metroruta.repository.EstacionRepository;
import com.metroruta.repository.LineaRepository;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    // los minutos son estimados, ver docs/RED_METRO.md
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
            if (datos.minutos().length != datos.estaciones().length - 1) {
                throw new IllegalStateException("Los minutos de " + datos.nombre() + " no cuadran con sus estaciones");
            }
            Linea linea = lineaRepository.save(new Linea(datos.nombre(), datos.color()));

            // una estacion que sale en dos lineas es la misma, ahi esta el transbordo
            for (String nombre : datos.estaciones()) {
                if (!estaciones.containsKey(nombre)) {
                    estaciones.put(nombre, estacionRepository.save(new Estacion(nombre)));
                }
            }

            for (int i = 0; i < datos.minutos().length; i++) {
                Estacion origen = estaciones.get(datos.estaciones()[i]);
                Estacion destino = estaciones.get(datos.estaciones()[i + 1]);
                conexionRepository.save(new Conexion(origen, destino, linea, datos.minutos()[i]));
                totalConexiones++;
            }
        }

        long ms = (System.nanoTime() - inicio) / 1_000_000;
        log.info("Red cargada: {} lineas, {} estaciones, {} conexiones en {} ms",
                RED.length, estaciones.size(), totalConexiones, ms);
        return true;
    }
}
