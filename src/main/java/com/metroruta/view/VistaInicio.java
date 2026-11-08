package com.metroruta.view;

import com.metroruta.dto.Ruta;
import com.metroruta.dto.Segmento;
import com.metroruta.model.CriterioRuta;
import com.metroruta.service.BusquedaService;
import com.metroruta.service.DetalleService;
import com.metroruta.service.HistorialService;
import com.metroruta.service.LlegadaService;
import com.metroruta.service.RutaService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.security.core.userdetails.UserDetails;

// pantalla principal: se elige origen, destino y criterio y se muestra la ruta
@Route("")
@PageTitle("Calcular ruta | MetroRuta")
@PermitAll
public class VistaInicio extends VerticalLayout {

    private final transient RutaService rutaService;
    private final transient DetalleService detalleService;
    private final transient LlegadaService llegadaService;
    private final transient HistorialService historialService;
    private final transient AuthenticationContext contexto;

    private final CampoEstacion origen;
    private final CampoEstacion destino;
    private final RadioButtonGroup<CriterioRuta> criterio = new RadioButtonGroup<>();
    private final Div resultado = new Div();

    public VistaInicio(BusquedaService busquedaService, RutaService rutaService, DetalleService detalleService,
                       LlegadaService llegadaService, HistorialService historialService,
                       AuthenticationContext contexto) {
        this.rutaService = rutaService;
        this.detalleService = detalleService;
        this.llegadaService = llegadaService;
        this.historialService = historialService;
        this.contexto = contexto;

        origen = new CampoEstacion(busquedaService, "Origen");
        destino = new CampoEstacion(busquedaService, "Destino");

        criterio.setLabel("Criterio");
        criterio.setItems(CriterioRuta.values());
        criterio.setItemLabelGenerator(Textos::criterio);
        criterio.setValue(CriterioRuta.RAPIDA);

        Button calcular = new Button("Calcular ruta", evento -> calcular());
        HorizontalLayout campos = new HorizontalLayout(origen, destino);
        add(new H2("Calcular ruta"), campos, criterio, calcular, resultado);
    }

    private void calcular() {
        resultado.removeAll();
        Optional<String> desde = origen.getSeleccion();
        Optional<String> hasta = destino.getSeleccion();
        if (desde.isEmpty() || hasta.isEmpty()) {
            mensaje("Elige el origen y el destino de la lista de sugerencias.");
            return;
        }
        try {
            Optional<Ruta> ruta = rutaService.calcular(desde.get(), hasta.get(), criterio.getValue());
            if (ruta.isEmpty()) {
                mensaje("No hay ruta disponible entre " + desde.get() + " y " + hasta.get()
                        + ". Puede que una estación del recorrido esté cerrada.");
                return;
            }
            guardarEnHistorial(ruta.get());
            mostrar(ruta.get());
        } catch (IllegalArgumentException e) {
            mensaje(e.getMessage());
        }
    }

    private void guardarEnHistorial(Ruta ruta) {
        contexto.getAuthenticatedUser(UserDetails.class)
                .ifPresent(usuario -> historialService.guardar(usuario.getUsername(), ruta));
    }

    private void mostrar(Ruta ruta) {
        LocalDateTime salida = llegadaService.ahora();
        LocalDateTime llegada = llegadaService.horaLlegada(salida, ruta);

        resultado.add(new H3(ruta.origen() + " → " + ruta.destino()));
        resultado.add(new Paragraph(ruta.minutos() + " min en total, " + ruta.transbordos() + " transbordos ("
                + Textos.criterio(ruta.criterio()) + ")"));
        resultado.add(new Paragraph("Saliendo ahora llegas a las " + llegadaService.formatear(salida, llegada)));
        for (Segmento segmento : detalleService.armar(ruta)) {
            if (segmento.minutosTransbordoPrevio() > 0) {
                resultado.add(new Paragraph("Transbordo en " + segmento.estaciones().get(0) + " ("
                        + segmento.minutosTransbordoPrevio() + " min caminando)"));
            }
            resultado.add(new Paragraph(segmento.linea() + ": " + String.join(" → ", segmento.estaciones())
                    + " (" + segmento.minutos() + " min)"));
        }
    }

    private void mensaje(String texto) {
        Paragraph parrafo = new Paragraph(texto);
        parrafo.getStyle().set("color", "#b00020");
        resultado.add(parrafo);
    }
}
