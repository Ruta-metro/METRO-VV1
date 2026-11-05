package com.metroruta.service;

import com.metroruta.model.Usuario;
import com.metroruta.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// le dice a Spring Security como buscar un usuario en la base de datos
@Service
public class DetallesUsuarioService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(DetallesUsuarioService.class);

    private final UsuarioRepository usuarioRepository;

    public DetallesUsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username).orElseThrow(() -> {
            log.warn("Intento de ingreso con un usuario que no existe: {}", username);
            return new UsernameNotFoundException("No existe el usuario " + username);
        });
        // roles("ADMIN") lo convierte en la autoridad ROLE_ADMIN
        return User.withUsername(usuario.getUsername())
                .password(usuario.getPassword())
                .roles(usuario.getRol().name())
                .build();
    }
}