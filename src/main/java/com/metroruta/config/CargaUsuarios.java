package com.metroruta.config;

import com.metroruta.model.Rol;
import com.metroruta.model.Usuario;
import com.metroruta.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// crea los usuarios de prueba (admin y usuario) al arrancar, si todavia no existen
@Component
public class CargaUsuarios implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CargaUsuarios.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder codificador;

    public CargaUsuarios(UsuarioRepository usuarioRepository, PasswordEncoder codificador) {
        this.usuarioRepository = usuarioRepository;
        this.codificador = codificador;
    }

    @Override
    public void run(ApplicationArguments args) {
        cargarSiFaltan();
    }

    // devuelve cuantos usuarios creo (0 si ya estaban los dos)
    public int cargarSiFaltan() {
        int creados = 0;
        if (crearSiNoExiste("admin", "admin123", Rol.ADMIN)) {
            creados++;
        }
        if (crearSiNoExiste("usuario", "usuario123", Rol.USUARIO)) {
            creados++;
        }
        return creados;
    }

    private boolean crearSiNoExiste(String username, String clave, Rol rol) {
        if (usuarioRepository.findByUsername(username).isPresent()) {
            return false;
        }
        // la clave se cifra antes de guardarla, nunca va en texto plano
        usuarioRepository.save(new Usuario(username, codificador.encode(clave), rol));
        log.info("Usuario inicial creado: {} con rol {}", username, rol);
        return true;
    }
}