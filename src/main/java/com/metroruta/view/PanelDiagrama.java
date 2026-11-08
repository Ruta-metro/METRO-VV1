package com.metroruta.view;

import com.metroruta.dto.Ruta;
import com.metroruta.service.DiagramaService;
import com.metroruta.service.DifusorEventos;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.Html;

// muestra el diagrama SVG y lo vuelve a dibujar solo cuando alguien cambia la red
public class PanelDiagrama extends Div {

    private final transient DiagramaService diagramaService;
    private final transient DifusorEventos difusor;
    private transient Ruta ruta;
    private transient Runnable cancelar;

    public PanelDiagrama(DiagramaService diagramaService, DifusorEventos difusor) {
        this.diagramaService = diagramaService;
        this.difusor = difusor;
        setWidthFull();
        getStyle().set("overflow-x", "auto");
    }

    // ruta null deja el diagrama sin resaltar
    public void mostrarRuta(Ruta nueva) {
        this.ruta = nueva;
        dibujar();
    }

    private void dibujar() {
        removeAll();
        add(new Html("<div>" + diagramaService.generarSvg(ruta) + "</div>"));
    }

    @Override
    protected void onAttach(AttachEvent evento) {
        dibujar();
        UI ui = evento.getUI();
        cancelar = difusor.suscribir(mensaje -> ui.access(this::dibujar));
    }

    @Override
    protected void onDetach(DetachEvent evento) {
        if (cancelar != null) {
            cancelar.run();
            cancelar = null;
        }
    }
}
