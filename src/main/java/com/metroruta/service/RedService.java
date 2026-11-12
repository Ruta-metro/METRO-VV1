package com.metroruta.service;

import com.metroruta.model.Conexion;
import com.metroruta.model.Estacion;
import com.metroruta.repository.ConexionRepository;
import com.metroruta.repository.EstacionRepository;
import com.metroruta.structures.Grafo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// arma el grafo en memoria a partir de la base de datos
@Service
public class RedService {

    private static final Logger log = LoggerFactory.getLogger(RedService.class);

    // minutos de caminata de un transbordo (estimado entre 3 y 5)
    public static final int MINUTOS_TRANSBORDO = 4;
    private final int minutosTransbordo;

    private final EstacionRepository estacionRepository;
    private final ConexionRepository conexionRepository;

    // volatile: las demas sesiones ven el grafo nuevo apenas se reemplaza
    private volatile Grafo grafo = new Grafo();

    public RedService(EstacionRepository estacionRepository, ConexionRepository conexionRepository,
                      @Value("${metroruta.transbordo.minutos:" + MINUTOS_TRANSBORDO + "}") int minutosTransbordo) {
        if (minutosTransbordo < 3 || minutosTransbordo > 5) {
            throw new IllegalArgumentException("El tiempo de transbordo debe estar entre 3 y 5 minutos");
        }
        this.minutosTransbordo = minutosTransbordo;
        this.estacionRepository = estacionRepository;
        this.conexionRepository = conexionRepository;
    }

    // cada nodo del grafo es una pareja (estacion, linea)
    public static String idNodo(String estacion, String linea) {
        return estacion + "|" + linea;
    }

    // las dos partes de un nodo
    public static String estacionDe(String nodo) {
        return nodo.substring(0, nodo.indexOf('|'));
    }

    public static String lineaDe(String nodo) {
        return nodo.substring(nodo.indexOf('|') + 1);
    }

    public Grafo getGrafo() {
        return grafo;
    }

    // se ejecuta cuando la app ya arranco, o sea despues de cargar la red inicial
    @EventListener(ApplicationReadyEvent.class)
    public void alIniciar() {
        reconstruir();
    }

    // arma un grafo nuevo desde la base de datos y lo reemplaza de una sola vez,
    // asi quien este calculando una ruta no ve un grafo a medio armar
    public void reconstruir() {
        long inicio = System.nanoTime();
        Grafo nuevo = new Grafo();
        Map<String, Set<String>> lineasPorEstacion = new HashMap<>();

        // un tramo une dos nodos de la misma linea, en los dos sentidos
        List<Conexion> conexiones = conexionRepository.findAll();
        for (Conexion c : conexiones) {
            String linea = c.getLinea().getNombre();
            String origen = idNodo(c.getOrigen().getNombre(), linea);
            String destino = idNodo(c.getDestino().getNombre(), linea);
            nuevo.agregarNodo(origen);
            nuevo.agregarNodo(destino);
            nuevo.conectar(origen, destino, c.getMinutos(), false);
            lineasPorEstacion.computeIfAbsent(c.getOrigen().getNombre(), k -> new HashSet<>()).add(linea);
            lineasPorEstacion.computeIfAbsent(c.getDestino().getNombre(), k -> new HashSet<>()).add(linea);
        }

        // transbordo: une los nodos de una misma estacion que esta en varias lineas
        int transbordos = 0;
        for (Map.Entry<String, Set<String>> entrada : lineasPorEstacion.entrySet()) {
            List<String> lineas = new ArrayList<>(entrada.getValue());
            for (int i = 0; i < lineas.size(); i++) {
                for (int j = i + 1; j < lineas.size(); j++) {
                    nuevo.conectar(idNodo(entrada.getKey(), lineas.get(i)),
                            idNodo(entrada.getKey(), lineas.get(j)), minutosTransbordo, true);
                    transbordos++;
                }
            }
        }

        // una estacion cerrada cierra todos sus nodos, uno por cada linea
        int nodosCerrados = 0;
        for (Estacion estacion : estacionRepository.findAll()) {
            if (estacion.isCerrada() && lineasPorEstacion.containsKey(estacion.getNombre())) {
                for (String linea : lineasPorEstacion.get(estacion.getNombre())) {
                    nuevo.cerrar(idNodo(estacion.getNombre(), linea));
                    nodosCerrados++;
                }
            }
        }

        grafo = nuevo;
        long ms = (System.nanoTime() - inicio) / 1_000_000;
        log.info("Grafo construido: {} nodos, {} tramos, {} transbordos, {} nodos cerrados en {} ms",
                nuevo.cantidadNodos(), conexiones.size(), transbordos, nodosCerrados, ms);
    }
}
