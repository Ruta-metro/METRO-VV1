package com.metroruta.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.metroruta.model.Rol;
import com.metroruta.model.Usuario;
import com.metroruta.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class DetallesUsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private DetallesUsuarioService servicio;

    private boolean tieneAutoridad(UserDetails detalles, String autoridad) {
        return detalles.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(autoridad));
    }

    @Test
    void adminTieneSusDatosYSuRol() {
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(new Usuario("admin", "hash", Rol.ADMIN)));
        UserDetails detalles = servicio.loadUserByUsername("admin");
        assertEquals("admin", detalles.getUsername());
        assertEquals("hash", detalles.getPassword());
        assertTrue(tieneAutoridad(detalles, "ROLE_ADMIN"));
    }

    @Test
    void usuarioNormalNoTieneRolAdmin() {
        when(usuarioRepository.findByUsername("usuario")).thenReturn(Optional.of(new Usuario("usuario", "hash", Rol.USUARIO)));
        UserDetails detalles = servicio.loadUserByUsername("usuario");
        assertTrue(tieneAutoridad(detalles, "ROLE_USUARIO"));
        assertFalse(tieneAutoridad(detalles, "ROLE_ADMIN"));
        assertEquals(1, detalles.getAuthorities().size());
    }

    @Test
    void usuarioQueNoExisteLanzaExcepcion() {
        when(usuarioRepository.findByUsername("fantasma")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> servicio.loadUserByUsername("fantasma"));
    }

    @Test
    void nombreVacioLanzaExcepcion() {
        when(usuarioRepository.findByUsername("")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> servicio.loadUserByUsername(""));
    }
}