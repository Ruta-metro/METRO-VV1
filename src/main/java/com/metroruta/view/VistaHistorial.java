package com.metroruta.view;

import com.metroruta.model.HistorialRuta;
import com.metroruta.service.HistorialService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.security.core.userdetails.UserDetails;

// las ultimas 20 rutas que consulto el usuario que tiene la sesion
@Route("historial")
@PageTitle("Historial | MetroRuta")
@PermitAll
public class VistaHistorial extends VerticalLayout {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public VistaHistorial(HistorialService historialService, AuthenticationContext contexto) {
        add(new H2("Historial de rutas"));
        String usuario = contexto.getAuthenticatedUser(UserDetails.class).map(UserDetails::getUsername).orElse(null);
        List<HistorialRuta> rutas = usuario == null ? List.of() : historialService.ultimas(usuario);
        if (rutas.isEmpty()) {
            add(new Paragraph("Todavía no has consultado ninguna ruta."));
            return;
        }
        Grid<HistorialRuta> tabla = new Grid<>();
        tabla.addColumn(r -> r.getFecha().format(FECHA)).setHeader("Fecha");
        tabla.addColumn(HistorialRuta::getOrigen).setHeader("Origen");
        tabla.addColumn(HistorialRuta::getDestino).setHeader("Destino");
        tabla.addColumn(r -> Textos.criterio(r.getCriterio())).setHeader("Criterio");
        tabla.addColumn(HistorialRuta::getMinutos).setHeader("Minutos");
        tabla.addColumn(HistorialRuta::getTransbordos).setHeader("Transbordos");
        tabla.setItems(rutas);
        add(tabla);
    }
}
