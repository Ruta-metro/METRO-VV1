package com.metroruta.view;

import com.metroruta.service.DiagramaService;
import com.metroruta.service.DifusorEventos;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

// la red completa; las estaciones cerradas salen en rojo
@Route("diagrama")
@PageTitle("Diagrama | MetroRuta")
@PermitAll
public class VistaDiagrama extends VerticalLayout {

    public VistaDiagrama(DiagramaService diagramaService, DifusorEventos difusor) {
        add(new H2("Diagrama de la red"), new PanelDiagrama(diagramaService, difusor));
    }
}
