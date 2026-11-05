package com.metroruta.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.userdetails.UserDetails;

// menu de arriba que se aplica solo a todas las vistas (menos el login)
@Layout
@PermitAll
public class DisenoPrincipal extends AppLayout {

    // transient porque AuthenticationContext no es serializable
    private final transient AuthenticationContext contexto;

    public DisenoPrincipal(AuthenticationContext contexto) {
        this.contexto = contexto;

        H1 titulo = new H1("MetroRuta");
        titulo.getStyle().set("margin", "0").set("font-size", "1.3rem");

        HorizontalLayout enlaces = new HorizontalLayout(titulo, new RouterLink("Inicio", VistaInicio.class));
        enlaces.setAlignItems(FlexComponent.Alignment.CENTER);
        // el enlace de administracion solo se muestra al admin (la proteccion real es @RolesAllowed)
        if (esAdmin()) {
            enlaces.add(new RouterLink("Administración", VistaAdmin.class));
        }

        String nombre = contexto.getAuthenticatedUser(UserDetails.class).map(UserDetails::getUsername).orElse("");
        Button salir = new Button("Cerrar sesión", evento -> contexto.logout());
        HorizontalLayout sesion = new HorizontalLayout(new Span(nombre), salir);
        sesion.setAlignItems(FlexComponent.Alignment.CENTER);

        HorizontalLayout encabezado = new HorizontalLayout(enlaces, sesion);
        encabezado.setWidthFull();
        encabezado.setAlignItems(FlexComponent.Alignment.CENTER);
        encabezado.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        encabezado.getStyle().set("padding", "0 1rem");
        addToNavbar(encabezado);
    }

    private boolean esAdmin() {
        return contexto.getAuthenticatedUser(UserDetails.class)
                .map(usuario -> usuario.getAuthorities().stream()
                        .anyMatch(autoridad -> autoridad.getAuthority().equals("ROLE_ADMIN")))
                .orElse(false);
    }
}
