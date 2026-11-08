package com.metroruta.view;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.junit.jupiter.api.Test;

class VistasAdminTest {

    @Test
    void lasAdministracionesSonSoloParaElAdmin() {
        for (Class<?> vista : new Class<?>[]{VistaEstaciones.class, VistaConexiones.class}) {
            RolesAllowed regla = vista.getAnnotation(RolesAllowed.class);
            assertNotNull(regla, vista.getSimpleName());
            assertArrayEquals(new String[]{"ADMIN"}, regla.value());
        }
    }

    @Test
    void elDiagramaLoVeCualquierUsuarioConSesion() {
        assertNotNull(VistaDiagrama.class.getAnnotation(PermitAll.class));
    }
}
