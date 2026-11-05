package com.metroruta.service;

import com.metroruta.model.Estacion;
import com.metroruta.repository.EstacionRepository;
import com.metroruta.structures.Trie;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

// busca estaciones por prefijo con el trie (es el autocompletado del buscador)
@Service
public class BusquedaService {

    private static final Logger log = LoggerFactory.getLogger(BusquedaService.class);

    private final EstacionRepository estacionRepository;

    // volatile: se reemplaza el trie completo, asi nadie lo ve a medio armar
    private volatile Trie trie = new Trie();

    public BusquedaService(EstacionRepository estacionRepository) {
        this.estacionRepository = estacionRepository;
    }

    // se ejecuta cuando la app ya arranco y la red inicial ya esta cargada
    @EventListener(ApplicationReadyEvent.class)
    public void alIniciar() {
        reconstruir();
    }

    // arma un trie nuevo con los nombres de la base de datos (tambien se llama al crear, editar o borrar estaciones)
    public void reconstruir() {
        long inicio = System.nanoTime();
        Trie nuevo = new Trie();
        List<Estacion> estaciones = estacionRepository.findAll();
        for (Estacion estacion : estaciones) {
            nuevo.insertar(estacion.getNombre());
        }
        trie = nuevo;
        long ms = (System.nanoTime() - inicio) / 1_000_000;
        log.info("Trie de estaciones armado: {} estaciones en {} ms", estaciones.size(), ms);
    }

    // nombres que empiezan con el texto; null da lista vacia y texto vacio da todas
    public List<String> buscar(String texto) {
        if (texto == null) {
            return List.of();
        }
        List<String> resultado = trie.buscarPorPrefijo(texto.trim());
        log.debug("Busqueda '{}': {} resultados", texto.trim(), resultado.size());
        return resultado;
    }
}