package com.metroruta.service;

import com.metroruta.model.Estacion;
import com.metroruta.repository.CierreRepository;
import com.metroruta.repository.ConexionRepository;
import com.metroruta.repository.EstacionRepository;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

// crear, editar y eliminar estaciones; cada cambio actualiza el trie, el grafo y avisa a las sesiones
@Service
public class EstacionService {

    private static final Logger log = LoggerFactory.getLogger(EstacionService.class);

    private static final int MAXIMO_NOMBRE = 60;

    private final EstacionRepository estacionRepository;
    private final ConexionRepository conexionRepository;
    private final CierreRepository cierreRepository;
    private final BusquedaService busquedaService;
    private final RedService redService;
    private final DiagramaService diagramaService;
    private final DifusorEventos difusor;

    public EstacionService(EstacionRepository estacionRepository, ConexionRepository conexionRepository,
                           CierreRepository cierreRepository, BusquedaService busquedaService,
                           RedService redService, DiagramaService diagramaService, DifusorEventos difusor) {
        this.estacionRepository = estacionRepository;
        this.conexionRepository = conexionRepository;
        this.cierreRepository = cierreRepository;
        this.busquedaService = busquedaService;
        this.redService = redService;
        this.diagramaService = diagramaService;
        this.difusor = difusor;
    }

    public List<Estacion> listar() {
        List<Estacion> lista = estacionRepository.findAll();
        lista.sort(Comparator.comparing(Estacion::getNombre, String.CASE_INSENSITIVE_ORDER));
        return lista;
    }

    // x e y pueden ser null: entonces se ubica sola en la fila de abajo del diagrama
    public synchronized Estacion crear(String nombre, Integer x, Integer y) {
        String limpio = validarNombre(nombre);
        if (estacionRepository.existsByNombreIgnoreCase(limpio)) {
            throw new IllegalStateException("Ya existe una estación llamada " + limpio);
        }
        Estacion estacion = new Estacion(limpio);
        ponerPosicion(estacion, x, y);
        estacion = estacionRepository.save(estacion);
        actualizarTodo("Se agregó la estación " + limpio);
        return estacion;
    }

    public synchronized Estacion editar(String actual, String nuevoNombre, Integer x, Integer y) {
        Estacion estacion = buscar(actual);
        String limpio = validarNombre(nuevoNombre);
        // cambiar solo mayusculas de la misma estacion es valido
        if (!limpio.equalsIgnoreCase(estacion.getNombre()) && estacionRepository.existsByNombreIgnoreCase(limpio)) {
            throw new IllegalStateException("Ya existe una estación llamada " + limpio);
        }
        estacion.setNombre(limpio);
        ponerPosicion(estacion, x, y);
        estacion = estacionRepository.save(estacion);
        actualizarTodo("Se actualizó la estación " + limpio);
        return estacion;
    }

    // borra tambien sus conexiones y su registro de cierres
    public synchronized void eliminar(String nombre) {
        Estacion estacion = buscar(nombre);
        conexionRepository.deleteByOrigenIdOrDestinoId(estacion.getId(), estacion.getId());
        cierreRepository.deleteByEstacionId(estacion.getId());
        estacionRepository.delete(estacion);
        actualizarTodo("Se eliminó la estación " + estacion.getNombre());
    }

    private Estacion buscar(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Falta la estación");
        }
        return estacionRepository.findByNombreIgnoreCase(nombre.trim())
                .orElseThrow(() -> new IllegalArgumentException("La estación " + nombre + " no existe"));
    }

    private String validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        String limpio = nombre.trim();
        if (limpio.length() > MAXIMO_NOMBRE) {
            throw new IllegalArgumentException("El nombre tiene más de " + MAXIMO_NOMBRE + " letras");
        }
        // la barra separa estacion y linea en los nodos del grafo
        if (limpio.contains("|")) {
            throw new IllegalArgumentException("El nombre no puede tener el símbolo |");
        }
        return limpio;
    }

    private void ponerPosicion(Estacion estacion, Integer x, Integer y) {
        if (x == null || y == null) {
            if (estacion.getPosX() == null || estacion.getPosY() == null) {
                int[] libre = diagramaService.posicionLibre();
                estacion.setPosX(libre[0]);
                estacion.setPosY(libre[1]);
            }
            return;
        }
        if (x < 0 || x > DiagramaService.ANCHO || y < 0 || y > DiagramaService.ALTO) {
            throw new IllegalArgumentException("La posición debe quedar dentro del diagrama (0 a "
                    + DiagramaService.ANCHO + " en X y 0 a " + DiagramaService.ALTO + " en Y)");
        }
        estacion.setPosX(x);
        estacion.setPosY(y);
    }

    private void actualizarTodo(String mensaje) {
        busquedaService.reconstruir();
        redService.reconstruir();
        difusor.emitir(mensaje);
        log.info(mensaje);
    }
}
