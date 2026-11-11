package com.metroruta.view;

import com.metroruta.dto.Ruta;
import com.metroruta.service.DiagramaService;
import com.metroruta.service.DifusorEventos;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
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
        ventana.addClassNames("map-viewport", "map-fit");
        ventana.getElement().setAttribute("tabindex", "0");
        ventana.getElement().setAttribute("role", "region");
        ventana.getElement().setAttribute("aria-label", "Mapa del Metro");
        ventana.add(lienzo);

        alejar.setAriaLabel("Alejar mapa");
        acercar.setAriaLabel("Acercar mapa");
        alejar.setEnabled(false);
        porcentaje.getElement().setAttribute("aria-live", "polite");
        Button ajustar = new Button("Ver todo", evento -> ajustarZoom(100));
        HorizontalLayout controles = new HorizontalLayout(alejar, porcentaje, acercar, ajustar);
        controles.addClassName("map-controls");
        controles.setAlignItems(HorizontalLayout.Alignment.CENTER);
        Paragraph fuentes = new Paragraph("Trazados: Metro de Medellín · Río: GeoMedellín · Relieve: SRTM / Mapzen");
        fuentes.addClassName("map-attribution");
        add(controles, ventana, fuentes);
    }

    // ruta null deja el diagrama sin resaltar
    public void mostrarRuta(Ruta nueva) {
        this.ruta = nueva;
        dibujar();
    }

    private void dibujar() {
        lienzo.removeAll();
        lienzo.add(new Html("<div>" + diagramaService.generarSvg(ruta) + "</div>"));
    }

    private void ajustarZoom(int nuevo) {
        zoom = nuevo;
        porcentaje.setText(zoom + " %");
        alejar.setEnabled(zoom > 100);
        acercar.setEnabled(zoom < 250);
        ventana.getElement().executeJs("""
                const centroX = (this.scrollLeft + this.clientWidth / 2) / this.scrollWidth;
                const centroY = (this.scrollTop + this.clientHeight / 2) / this.scrollHeight;
                this.classList.toggle('map-fit', $1 === 100);
                $0.style.width = $1 + '%';
                this.scrollLeft = centroX * this.scrollWidth - this.clientWidth / 2;
                this.scrollTop = $1 === 100 ? 0 : centroY * this.scrollHeight - this.clientHeight / 2;
                """, lienzo.getElement(), zoom);
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
