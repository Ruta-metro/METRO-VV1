package com.metroruta.view;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.annotation.security.PermitAll;
import org.junit.jupiter.api.Test;

class VistasTest {

    @Test
    void lasVistasDeUsuarioLasPuedeAbrirCualquierUsuarioConSesion() {
        assertNotNull(VistaInicio.class.getAnnotation(PermitAll.class));
        assertNotNull(VistaHistorial.class.getAnnotation(PermitAll.class));
    }
}
