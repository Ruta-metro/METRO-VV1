package com.metroruta.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.metroruta.model.Rol;
import com.metroruta.model.Usuario;
import com.metroruta.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class CargaUsuariosTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private final PasswordEncoder codificador = new BCryptPasswordEncoder();
    private CargaUsuarios carga;

    @BeforeEach
    void preparar() {
        carga = new CargaUsuarios(usuarioRepository, codificador);
    }

    private List<Usuario> guardados(int veces) {
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository, times(veces)).save(captor.capture());
        return captor.getAllValues();
    }

    @Test
    void creaAdminYUsuarioCuandoNoExisten() {
        when(usuarioRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        assertEquals(2, carga.cargarSiFaltan());
        List<Usuario> creados = guardados(2);
        assertEquals("admin", creados.get(0).getUsername());
        assertEquals(Rol.ADMIN, creados.get(0).getRol());
        assertEquals("usuario", creados.get(1).getUsername());
        assertEquals(Rol.USUARIO, creados.get(1).getRol());
    }

    @Test
    void laContrasenaSeGuardaCifrada() {
        when(usuarioRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        carga.cargarSiFaltan();
        Usuario admin = guardados(2).get(0);
        assertNotEquals("admin123", admin.getPassword());
        assertTrue(codificador.matches("admin123", admin.getPassword()));
    }

    @Test
    void noDuplicaSiYaExisten() {
        when(usuarioRepository.findByUsername(anyString())).thenReturn(Optional.of(new Usuario("x", "hash", Rol.USUARIO)));
        assertEquals(0, carga.cargarSiFaltan());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void creaSoloElQueFalta() {
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(new Usuario("admin", "hash", Rol.ADMIN)));
        when(usuarioRepository.findByUsername("usuario")).thenReturn(Optional.empty());
        assertEquals(1, carga.cargarSiFaltan());
        assertEquals("usuario", guardados(1).get(0).getUsername());
    }
}