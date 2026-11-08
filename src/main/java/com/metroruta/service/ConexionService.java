package com.metroruta.service;

import com.metroruta.model.Conexion;
import com.metroruta.model.Estacion;
import com.metroruta.model.Linea;
import com.metroruta.repository.ConexionRepository;
import com.metroruta.repository.EstacionRepository;
import com.metroruta.repository.LineaRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

// crear, cambiar y eliminar tramos entre estaciones; cada cambio rearma el grafo y avisa a las sesiones
@Service
public class ConexionService {

    private static final Logger log = LoggerFactory.getLogger(ConexionService.class);

    private static final int MAXIMO_MINUTOS = 120;

    private final ConexionRepository conexionRepository;
    private final EstacionRepository estacionRepository;
    private final LineaRepository lineaRepository;
    private final RedService redService;
    private final DifusorEventos difusor;

    public ConexionService(ConexionRepository conexionRepository, EstacionRepository estacionRepository,
                           LineaRepository lineaRepository, RedService redService, DifusorEventos difusor) {
        this.conexionRepository = conexionRepository;
        this.estacionRepository = estacionRepository;
        this.lineaRepository = lineaRepository;
        this.redService = redService;
        this.difusor = difusor;
    }

    public List<Conexion> listar() {
        List<Conexion> lista = new ArrayList<>(conexionRepository.findAll());
        lista.sort(Comparator.comparing((Conexion c) -> c.getLinea().getNombre())
                .thenComparing(c -> c.getOrigen().getNombre()));
        return lista;
    }

    public List<String> nombresDeLineas() {
        List<String> nombres = new ArrayList<>();
        for (Linea linea : lineaRepository.findAll()) {
            nombres.add(linea.getNombre());
        }
        nombres.sort(Comparator.naturalOrder());
        return nombres;
    }

    public List<String> nombresDeEstaciones() {
        List<String> nombres = new ArrayList<>();
        for (Estacion estacion : estacionRepository.findAll()) {
            nombres.add(estacion.getNombre());
        }
        nombres.sort(String.CASE_INSENSITIVE_ORDER);
        return nombres;
    }

    public synchronized Conexion crear(String origen, String destino, String linea, int minutos) {
        validarMinutos(minutos);
        Estacion desde = estacion(origen);
        Estacion hasta = estacion(destino);
        if (desde.equals(hasta)) {
            throw new IllegalArgumentException("El origen y el destino deben ser distintos");
        }
        Linea laLinea = lineaRepository.findByNombre(linea == null ? "" : linea.trim())
                .orElseThrow(() -> new IllegalArgumentException("La línea " + linea + " no existe"));
        for (Conexion c : conexionRepository.findAll()) {
            boolean mismaLinea = c.getLinea().equals(laLinea);
            boolean mismoPar = (c.getOrigen().equals(desde) && c.getDestino().equals(hasta))
                    || (c.getOrigen().equals(hasta) && c.getDestino().equals(desde));
            if (mismaLinea && mismoPar) {
                throw new IllegalStateException("Ya existe un tramo entre " + desde.getNombre() + " y "
                        + hasta.getNombre() + " en " + laLinea.getNombre());
            }
        }
        Conexion conexion = conexionRepository.save(new Conexion(desde, hasta, laLinea, minutos));
        actualizar("Nuevo tramo " + desde.getNombre() + " - " + hasta.getNombre() + " en " + laLinea.getNombre());
        return conexion;
    }

    public synchronized Conexion cambiarMinutos(Long id, int minutos) {
        validarMinutos(minutos);
        Conexion conexion = buscar(id);
        conexion.setMinutos(minutos);
        conexion = conexionRepository.save(conexion);
        actualizar("Cambió el tiempo del tramo " + conexion.getOrigen().getNombre() + " - "
                + conexion.getDestino().getNombre());
        return conexion;
    }

    public synchronized void eliminar(Long id) {
        Conexion conexion = buscar(id);
        conexionRepository.delete(conexion);
        actualizar("Se eliminó el tramo " + conexion.getOrigen().getNombre() + " - " + conexion.getDestino().getNombre());
    }

    private Conexion buscar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Falta elegir el tramo");
        }
        return conexionRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("El tramo no existe"));
    }

    private Estacion estacion(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Falta una estación");
        }
        return estacionRepository.findByNombreIgnoreCase(nombre.trim())
                .orElseThrow(() -> new IllegalArgumentException("La estación " + nombre + " no existe"));
    }

    private void validarMinutos(int minutos) {
        if (minutos < 1 || minutos > MAXIMO_MINUTOS) {
            throw new IllegalArgumentException("Los minutos deben estar entre 1 y " + MAXIMO_MINUTOS);
        }
    }

    private void actualizar(String mensaje) {
        redService.reconstruir();
        difusor.emitir(mensaje);
        log.info(mensaje);
    }
}
