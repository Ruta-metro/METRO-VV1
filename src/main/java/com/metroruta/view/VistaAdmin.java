package com.metroruta.view;

import com.metroruta.service.BusquedaService;
import com.metroruta.service.CierreService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;
import java.util.Optional;
import java.util.function.Consumer;
import org.springframework.security.core.userdetails.UserDetails;

// solo el admin entra aqui: cerrar, reabrir y deshacer cierres de estaciones
@Route("admin")
@PageTitle("Administración | MetroRuta")
@RolesAllowed("ADMIN")
public class VistaAdmin extends VerticalLayout {

    private final transient CierreService cierreService;
    private final transient AuthenticationContext contexto;

    private final CampoEstacion campo;
    private final Button deshacer = new Button("Deshacer último cambio");
    private final Grid<String> cerradas = new Grid<>();

    public VistaAdmin(BusquedaService busquedaService, CierreService cierreService, AuthenticationContext contexto) {
        this.cierreService = cierreService;
        this.contexto = contexto;
        campo = new CampoEstacion(busquedaService, "Estación");

        Button cerrar = new Button("Cerrar estación", evento -> cambiar(
                nombre -> cierreService.cerrar(nombre, usuario()), "Estación cerrada: "));
        Button reabrir = new Button("Reabrir estación", evento -> cambiar(
                nombre -> cierreService.reabrir(nombre, usuario()), "Estación reabierta: "));
        deshacer.addClickListener(evento -> deshacerUltimo());

        cerradas.addColumn(nombre -> nombre).setHeader("Estaciones cerradas");
        add(new H2("Administración"), new H3("Cierres de estaciones"), campo,
                new HorizontalLayout(cerrar, reabrir, deshacer), cerradas);
        refrescar();
    }

    private String usuario() {
        return contexto.getAuthenticatedUser(UserDetails.class).map(UserDetails::getUsername).orElse("admin");
    }

    private void cambiar(Consumer<String> accion, String textoOk) {
        Optional<String> seleccion = campo.getSeleccion();
        if (seleccion.isEmpty()) {
            Notification.show("Elige una estación de la lista de sugerencias");
            return;
        }
        try {
            accion.accept(seleccion.get());
            Notification.show(textoOk + seleccion.get());
        } catch (IllegalArgumentException | IllegalStateException e) {
            Notification.show(e.getMessage());
        }
        refrescar();
    }

    private void deshacerUltimo() {
        try {
            Notification.show(cierreService.deshacer(usuario()));
        } catch (IllegalStateException | IllegalArgumentException e) {
            Notification.show(e.getMessage());
        }
        refrescar();
    }

    // la lista de cerradas y el boton de deshacer siempre reflejan el estado actual
    private void refrescar() {
        cerradas.setItems(cierreService.cerradas());
        deshacer.setEnabled(cierreService.puedeDeshacer());
    }
}
