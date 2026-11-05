package com.metroruta.view;

import com.metroruta.service.BusquedaService;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

// pantalla para buscar estaciones por nombre
@Route("buscar")
@PageTitle("Buscar estaciones | MetroRuta")
@PermitAll
public class VistaBuscador extends VerticalLayout {

    public VistaBuscador(BusquedaService busquedaService) {
        Span elegida = new Span();
        CampoEstacion campo = new CampoEstacion(busquedaService, "Estación");
        campo.setAlElegir(nombre -> elegida.setText("Estación elegida: " + nombre));
        add(new H2("Buscar estaciones"), campo, elegida);
    }
}