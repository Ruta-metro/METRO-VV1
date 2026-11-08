package com.metroruta.service;

import com.metroruta.dto.Ruta;
import com.metroruta.model.CriterioRuta;
import com.metroruta.model.Estacion;
import com.metroruta.repository.EstacionRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

// calcula rutas sobre el grafo que mantiene RedService
@Service
public class RutaService {

    private static final Logger log = LoggerFactory.getLogger(RutaService.class);

    private final RedService redService;
    private final EstacionRepository estacionRepository;

    public RutaService(RedService redService, EstacionRepository estacionRepository) {
        this.redService = redService;
        this.estacionRepository = estacionRepository;
    }

    // vacio significa que no hay camino (por ejemplo, hay una estacion cerrada en medio)
    public Optional<Ruta> calcular(String origen, String destino, CriterioRuta criterio) {
        if (criterio == null) {
            throw new IllegalArgumentException("Falta el criterio de la ruta");
        }
        String desde = nombreReal(origen);
        String hasta = nombreReal(destino);

        long inicio = System.nanoTime();
        Optional<Ruta> ruta = Dijkstra.calcular(redService.getGrafo(), desde, hasta, criterio);
        long micros = (System.nanoTime() - inicio) / 1000;

        if (ruta.isPresent()) {
            log.info("Ruta {} -> {} ({}): {} min, {} transbordos, calculada en {} us",
                    desde, hasta, criterio, ruta.get().minutos(), ruta.get().transbordos(), micros);
        } else {
            log.info("Sin ruta {} -> {} ({}), busqueda de {} us", desde, hasta, criterio, micros);
        }
        return ruta;
    }

    // el nombre tal como esta en la base de datos (ignora mayusculas)
    private String nombreReal(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Falta una estacion");
        }
        Optional<Estacion> estacion = estacionRepository.findByNombreIgnoreCase(nombre.trim());
        if (estacion.isEmpty()) {
            throw new IllegalArgumentException("La estacion " + nombre + " no existe");
        }
        return estacion.get().getNombre();
    }
}
