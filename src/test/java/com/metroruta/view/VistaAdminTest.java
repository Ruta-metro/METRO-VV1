package com.metroruta.view;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.security.RolesAllowed;
import org.junit.jupiter.api.Test;

class VistaAdminTest {

    @Test
    void soloElAdminPuedeAbrirLaVistaDeAdministracion() {
        RolesAllowed regla = VistaAdmin.class.getAnnotation(RolesAllowed.class);
        assertNotNull(regla);
        assertArrayEquals(new String[]{"ADMIN"}, regla.value());
    }

    @Test
    void elLoginEsAccesibleSinSesion() {
        assertNotNull(VistaLogin.class.getAnnotation(AnonymousAllowed.class));
    }
}