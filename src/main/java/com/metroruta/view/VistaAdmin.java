package com.metroruta.view;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

// solo el admin entra aqui; despues se llena con los cierres y la administracion de la red
@Route("admin")
@PageTitle("Administración | MetroRuta")
@RolesAllowed("ADMIN")
public class VistaAdmin extends VerticalLayout {

    public VistaAdmin() {
        add(new H2("Administración"), new Paragraph("Aquí irán los cierres de estaciones y la administración de la red."));
    }
}