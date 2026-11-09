package com.metroruta.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.stereotype.Component;

// deja en el log quien entra, quien sale y los intentos fallidos (la clave nunca se registra)
@Component
public class RegistroSesiones {

    private static final Logger log = LoggerFactory.getLogger(RegistroSesiones.class);

    @EventListener
    public void alIngresar(AuthenticationSuccessEvent evento) {
        log.info("Ingreso correcto: {}", evento.getAuthentication().getName());
    }

    // el tipo de error dice si fue clave mala, cuenta bloqueada, etc.
    @EventListener
    public void alFallar(AbstractAuthenticationFailureEvent evento) {
        log.warn("Ingreso fallido de {}: {}", evento.getAuthentication().getName(),
                evento.getException().getClass().getSimpleName());
    }

    @EventListener
    public void alSalir(LogoutSuccessEvent evento) {
        log.info("Cierre de sesión: {}", evento.getAuthentication().getName());
    }
}