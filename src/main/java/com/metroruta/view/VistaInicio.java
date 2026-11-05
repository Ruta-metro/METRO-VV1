package com.metroruta.view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route("")
@PermitAll
public class VistaInicio extends VerticalLayout {

    public VistaInicio() {
        add(new H1("MetroRuta Medellín"));
    }
}
