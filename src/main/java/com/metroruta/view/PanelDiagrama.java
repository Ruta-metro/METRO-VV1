package com.metroruta.view;

import com.metroruta.dto.Ruta;
import com.metroruta.service.DiagramaService;
import com.metroruta.service.DifusorEventos;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.Html;

// muestra el diagrama SVG y lo vuelve a dibujar solo cuando alguien cambia la red
public class PanelDiagrama extends Div {

    private final transient DiagramaService diagramaService;
    private final transient DifusorEventos difusor;
    private final Div ventana = new Div();
    private final Div lienzo = new Div();
    private final Div leyenda = new Div();
    private int zoom = 100;
    private final Span porcentaje = new Span("100 %");
    private final Button alejar = new Button("−", evento -> ajustarZoom(zoom - 50));
    private final Button acercar = new Button("+", evento -> ajustarZoom(zoom + 50));
    private transient Ruta ruta;
    private transient Runnable cancelar;

    public PanelDiagrama(DiagramaService diagramaService, DifusorEventos difusor) {
        this.diagramaService = diagramaService;
        this.difusor = difusor;
        setWidthFull();
        addClassName("metro-map");
        ventana.addClassName("map-viewport");
        ventana.getElement().setAttribute("tabindex", "0");
        ventana.getElement().setAttribute("role", "region");
        ventana.getElement().setAttribute("aria-label", "Mapa del Metro");
        lienzo.addClassName("map-canvas");
        Span norte = new Span("↑ N");
        norte.addClassName("map-north");
        norte.getElement().setAttribute("aria-label", "Norte hacia arriba");
        ventana.add(lienzo, norte);

        alejar.setAriaLabel("Alejar mapa");
        acercar.setAriaLabel("Acercar mapa");
        alejar.setEnabled(false);
        porcentaje.getElement().setAttribute("aria-live", "polite");
        Button ajustar = new Button("Ver todo", evento -> ajustarZoom(100));
        HorizontalLayout controles = new HorizontalLayout(alejar, porcentaje, acercar, ajustar);
        controles.addClassName("map-controls");
        controles.setAlignItems(HorizontalLayout.Alignment.CENTER);
        leyenda.addClassName("map-legend-list");
        Details detalles = new Details("Leyenda de líneas", leyenda);
        detalles.addClassName("map-legend");
        controles.add(detalles);
        Paragraph ayuda = new Paragraph("Acerca y arrastra para explorar. Pasa sobre una estación para ver su nombre.");
        ayuda.addClassName("map-help");
        Paragraph fuentes = new Paragraph("Trazados: Metro de Medellín · Río: GeoMedellín · Relieve: SRTM / Mapzen");
        fuentes.addClassName("map-attribution");
        add(controles, ventana, ayuda, fuentes);
    }

    // ruta null deja el diagrama sin resaltar
    public void mostrarRuta(Ruta nueva) {
        this.ruta = nueva;
        dibujar();
    }

    private void dibujar() {
        lienzo.removeAll();
        DiagramaService.Mapa mapa = diagramaService.generarMapa(ruta);
        lienzo.add(new Html("<div>" + mapa.svg() + "</div>"));
        leyenda.removeAll();
        mapa.lineas().forEach(this::agregarLineaALeyenda);
        agregarLineaALeyenda("Estación cerrada", "#d32f2f");
        ventana.getElement().executeJs("import('/mapa/metro-map.js').then(m => m.iniciar(this, $0))", zoom);
    }

    private void agregarLineaALeyenda(String nombre, String color) {
        Span muestra = new Span();
        muestra.addClassName("map-legend-color");
        muestra.getElement().setAttribute("aria-hidden", "true");
        muestra.getStyle().set("background-color", color);
        Div fila = new Div(muestra, new Span(nombre));
        fila.addClassName("map-legend-item");
        leyenda.add(fila);
    }

    private void ajustarZoom(int nuevo) {
        zoom = Math.max(100, Math.min(250, nuevo));
        porcentaje.setText(zoom + " %");
        alejar.setEnabled(zoom > 100);
        acercar.setEnabled(zoom < 250);
        ventana.getElement().executeJs("import('/mapa/metro-map.js').then(m => m.ajustarZoom(this, $0))", zoom);
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
