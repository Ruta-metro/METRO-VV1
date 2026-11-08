package com.metroruta.service;

import com.metroruta.model.AccionCierre;
import com.metroruta.model.Cierre;
import com.metroruta.model.Estacion;
import com.metroruta.repository.CierreRepository;
import com.metroruta.repository.EstacionRepository;
import com.metroruta.structures.Pila;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

// cierra y reabre estaciones; cada cambio va a una pila para poder deshacerlo
@Service
public class CierreService {

    private static final Logger log = LoggerFactory.getLogger(CierreService.class);

    // lo que se hizo, para poder hacer lo contrario
    private record Accion(String estacion, AccionCierre accion) {
    }

    private final EstacionRepository estacionRepository;
    private final CierreRepository cierreRepository;
    private final RedService redService;
    private final DifusorEventos difusor;

    private Pila<Accion> acciones = new Pila<>();

    public CierreService(EstacionRepository estacionRepository, CierreRepository cierreRepository,
                         RedService redService, DifusorEventos difusor) {
        this.estacionRepository = estacionRepository;
        this.cierreRepository = cierreRepository;
        this.redService = redService;
        this.difusor = difusor;
    }

    // synchronized: dos admins a la vez no deben mezclar sus cambios
    public synchronized void cerrar(String nombre, String usuario) {
        cambiar(nombre, usuario, true, true);
    }

    public synchronized void reabrir(String nombre, String usuario) {
        cambiar(nombre, usuario, false, true);
    }

    // devuelve un texto con lo que se deshizo
    public synchronized String deshacer(String usuario) {
        if (acciones.estaVacia()) {
            throw new IllegalStateException("No hay nada que deshacer");
        }
        Accion ultima = acciones.desapilar();
        boolean cerrar = ultima.accion() == AccionCierre.REABRIR;
        cambiar(ultima.estacion(), usuario, cerrar, false);
        return (cerrar ? "Se volvió a cerrar " : "Se volvió a abrir ") + ultima.estacion();
    }

    public synchronized boolean puedeDeshacer() {
        return !acciones.estaVacia();
    }

    // se usa en las pruebas para empezar con la pila limpia
    public synchronized void vaciarHistorial() {
        acciones = new Pila<>();
    }

    public List<String> cerradas() {
        List<String> nombres = new ArrayList<>();
        for (Estacion estacion : estacionRepository.findAll()) {
            if (estacion.isCerrada()) {
                nombres.add(estacion.getNombre());
            }
        }
        nombres.sort(Comparator.naturalOrder());
        return nombres;
    }

    private void cambiar(String nombre, String usuario, boolean cerrar, boolean registrar) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Falta la estación");
        }
        if (usuario == null || usuario.isBlank()) {
            throw new IllegalArgumentException("Falta el usuario");
        }
        Estacion estacion = estacionRepository.findByNombreIgnoreCase(nombre.trim())
                .orElseThrow(() -> new IllegalArgumentException("La estación " + nombre + " no existe"));
        if (estacion.isCerrada() == cerrar) {
            throw new IllegalStateException("La estación " + estacion.getNombre()
                    + (cerrar ? " ya está cerrada" : " ya está abierta"));
        }

        estacion.setCerrada(cerrar);
        estacionRepository.save(estacion);
        AccionCierre accion = cerrar ? AccionCierre.CERRAR : AccionCierre.REABRIR;
        cierreRepository.save(new Cierre(estacion, accion, usuario));
        if (registrar) {
            acciones.apilar(new Accion(estacion.getNombre(), accion));
        }

        // el grafo se arma de nuevo y las rutas siguientes ya cuentan con el cambio
        redService.reconstruir();
        difusor.emitir("La estación " + estacion.getNombre() + (cerrar ? " fue cerrada" : " fue reabierta"));
        log.info("{} {} la estación {}", usuario, cerrar ? "cerró" : "reabrió", estacion.getNombre());
    }
}
