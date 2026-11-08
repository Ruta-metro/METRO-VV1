package com.metroruta.service;

import com.metroruta.dto.Ruta;
import com.metroruta.model.HistorialRuta;
import com.metroruta.model.Usuario;
import com.metroruta.repository.HistorialRutaRepository;
import com.metroruta.repository.UsuarioRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

// guarda las rutas que consulta cada usuario y devuelve las ultimas 20
@Service
public class HistorialService {

    private static final Logger log = LoggerFactory.getLogger(HistorialService.class);

    private final HistorialRutaRepository historialRepository;
    private final UsuarioRepository usuarioRepository;

    public HistorialService(HistorialRutaRepository historialRepository, UsuarioRepository usuarioRepository) {
        this.historialRepository = historialRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public HistorialRuta guardar(String username, Ruta ruta) {
        if (ruta == null) {
            throw new IllegalArgumentException("No hay ruta para guardar");
        }
        Usuario usuario = buscarUsuario(username);
        HistorialRuta registro = historialRepository.save(new HistorialRuta(usuario, ruta.origen(),
                ruta.destino(), ruta.criterio(), ruta.minutos(), ruta.transbordos()));
        log.info("Historial: {} consulto {} -> {} ({})", username, ruta.origen(), ruta.destino(), ruta.criterio());
        return registro;
    }

    // las mas recientes primero, maximo 20
    public List<HistorialRuta> ultimas(String username) {
        return historialRepository.findTop20ByUsuarioIdOrderByFechaDesc(buscarUsuario(username).getId());
    }

    private Usuario buscarUsuario(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Falta el usuario");
        }
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("No existe el usuario " + username));
    }
}
