package com.metroruta.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;

class RegistroSesionesTest {

    private final RegistroSesiones registro = new RegistroSesiones();
    private final Logger logger = (Logger) LoggerFactory.getLogger(RegistroSesiones.class);
    private ListAppender<ILoggingEvent> lista;

    @BeforeEach
    void preparar() {
        lista = new ListAppender<>();
        lista.start();
        logger.addAppender(lista);
    }

    @AfterEach
    void limpiar() {
        logger.detachAppender(lista);
    }

    private String primerMensaje() {
        assertEquals(1, lista.list.size());
        return lista.list.get(0).getFormattedMessage();
    }

    @Test
    void elIngresoCorrectoDejaElUsuarioPeroNoLaClave() {
        registro.alIngresar(new AuthenticationSuccessEvent(new UsernamePasswordAuthenticationToken("admin", "secreto")));
        String mensaje = primerMensaje();
        assertTrue(mensaje.contains("admin"));
        assertFalse(mensaje.contains("secreto"));
        assertEquals(Level.INFO, lista.list.get(0).getLevel());
    }

    @Test
    void elIntentoFallidoSeRegistraComoAdvertencia() {
        registro.alFallar(new AuthenticationFailureBadCredentialsEvent(
                new UsernamePasswordAuthenticationToken("admin", "hola"), new BadCredentialsException("mal")));
        String mensaje = primerMensaje();
        assertTrue(mensaje.contains("admin"));
        assertTrue(mensaje.contains("BadCredentialsException"));
        assertFalse(mensaje.contains("hola"));
        assertEquals(Level.WARN, lista.list.get(0).getLevel());
    }

    @Test
    void elCierreDeSesionDejaElUsuario() {
        registro.alSalir(new LogoutSuccessEvent(new UsernamePasswordAuthenticationToken("usuario", null)));
        assertTrue(primerMensaje().contains("usuario"));
    }

    @Test
    void unUsuarioSinNombreNoRompeElLog() {
        registro.alIngresar(new AuthenticationSuccessEvent(new UsernamePasswordAuthenticationToken(null, null)));
        assertEquals(1, lista.list.size());
    }
}