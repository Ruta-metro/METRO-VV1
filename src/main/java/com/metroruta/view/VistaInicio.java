package com.metroruta.view;

import com.metroruta.dto.Ruta;
import com.metroruta.dto.Segmento;
import com.metroruta.model.CriterioRuta;
import com.metroruta.service.BusquedaService;
import com.metroruta.service.DetalleService;
import com.metroruta.service.DiagramaService;
import com.metroruta.service.DifusorEventos;
import com.metroruta.service.HistorialService;
import com.metroruta.service.LlegadaService;
import com.metroruta.service.RutaService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
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

// pantalla principal: se elige origen, destino y criterio; muestra la ruta, el detalle y el diagrama resaltado
@Route("")
@PageTitle("Calcular ruta | MetroRuta")
@PermitAll
public class VistaInicio extends VerticalLayout {

    // la ultima consulta, para recalcularla si la red cambia
    private record Consulta(String origen, String destino, CriterioRuta criterio) {
    }

    private final transient RutaService rutaService;
    private final transient DetalleService detalleService;
    private final transient LlegadaService llegadaService;
    private final transient HistorialService historialService;
    private final transient DifusorEventos difusor;
    private final transient AuthenticationContext contexto;

    private final CampoEstacion origen;
    private final CampoEstacion destino;
    private final RadioButtonGroup<CriterioRuta> criterio = new RadioButtonGroup<>();
    private final Div resultado = new Div();
    private final PanelDiagrama diagrama;

    private transient Consulta ultimaConsulta;
    private transient Ruta ultimaRuta;
    private transient Runnable cancelar;

    public VistaInicio(BusquedaService busquedaService, RutaService rutaService, DetalleService detalleService,
                       LlegadaService llegadaService, HistorialService historialService,
                       DiagramaService diagramaService, DifusorEventos difusor, AuthenticationContext contexto) {
        this.rutaService = rutaService;
        this.detalleService = detalleService;
        this.llegadaService = llegadaService;
        this.historialService = historialService;
        this.difusor = difusor;
        this.contexto = contexto;

        origen = new CampoEstacion(busquedaService, "Origen");
        destino = new CampoEstacion(busquedaService, "Destino");
        diagrama = new PanelDiagrama(diagramaService, difusor);

        criterio.setLabel("Criterio");
        criterio.setItems(CriterioRuta.values());
        criterio.setItemLabelGenerator(Textos::criterio);
        criterio.setValue(CriterioRuta.RAPIDA);

        addClassName("metro-home");
        Button calcular = new Button("Calcular ruta", evento -> calcular());
        HorizontalLayout campos = new HorizontalLayout(origen, destino);
        calcular.addClassName("primary-action");
        campos.addClassName("station-fields");
        resultado.addClassName("route-result");
        resultado.getElement().setAttribute("aria-live", "polite");
        Div hero = new Div(new Paragraph("MEDELLÍN · UN VALLE, MUCHOS CAMINOS"),
                new H2("Tu próximo destino empieza aquí."),
                new Paragraph("Conecta la ciudad a tu manera. Encuentra tu recorrido en metro, tranvía y metrocable."));
        hero.addClassName("metro-hero");
        Div planificador = new Div(new Paragraph("01 / PLANEA TU VIAJE"), new H3("¿A dónde vamos?"),
                campos, criterio, calcular, resultado);
        planificador.addClassName("planner-card");
        Div mapa = new Div(new H3("Mapa del Metro"),
                new Paragraph("Tu recorrido aparecerá resaltado en el mapa. Las estaciones cerradas se muestran en rojo."), diagrama);
        mapa.addClassName("map-card");
        Div contenido = new Div(planificador, mapa);
        contenido.addClassName("planner-grid");
        Paragraph pie = new Paragraph("MetroRuta Medellín · Proyecto académico · Tiempos de viaje estimados");
        pie.addClassName("metro-footer");
        add(hero, contenido, pie);
    }

    private void calcular() {
        resultado.removeAll();
        Optional<String> desde = origen.getSeleccion();
        Optional<String> hasta = destino.getSeleccion();
        if (desde.isEmpty() || hasta.isEmpty()) {
            mensaje("Elige el origen y el destino de la lista de sugerencias.");
            return;
        }
        ultimaConsulta = new Consulta(desde.get(), hasta.get(), criterio.getValue());
        try {
            Optional<Ruta> ruta = rutaService.calcular(desde.get(), hasta.get(), criterio.getValue());
            if (ruta.isEmpty()) {
                sinRuta();
                return;
            }
            guardarEnHistorial(ruta.get());
            mostrar(ruta.get());
        } catch (IllegalArgumentException e) {
            ultimaConsulta = null;
            ultimaRuta = null;
            diagrama.mostrarRuta(null);
            mensaje(e.getMessage());
        }
    }

    private void sinRuta() {
        ultimaRuta = null;
        diagrama.mostrarRuta(null);
        mensaje("No hay ruta disponible entre " + ultimaConsulta.origen() + " y " + ultimaConsulta.destino()
                + ". Puede que una estación del recorrido esté cerrada.");
    }

    private void guardarEnHistorial(Ruta ruta) {
        contexto.getAuthenticatedUser(UserDetails.class)
                .ifPresent(usuario -> historialService.guardar(usuario.getUsername(), ruta));
    }

    private void mostrar(Ruta ruta) {
        ultimaRuta = ruta;
        diagrama.mostrarRuta(ruta);
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

    // alguien cambio la red: se avisa y, si la ruta que se ve cambia, se recalcula
    private void alCambiarRed(String aviso) {
        Notification.show(aviso);
        if (ultimaConsulta == null) {
            return;
        }
        try {
            Optional<Ruta> nueva = rutaService.calcular(ultimaConsulta.origen(), ultimaConsulta.destino(),
                    ultimaConsulta.criterio());
            if (nueva.isEmpty()) {
                if (ultimaRuta != null) {
                    resultado.removeAll();
                    sinRuta();
                }
            } else if (ultimaRuta == null || !nueva.get().pasos().equals(ultimaRuta.pasos())) {
                resultado.removeAll();
                Paragraph nota = new Paragraph("La ruta cambió: " + aviso);
                nota.getStyle().set("font-weight", "bold");
                resultado.add(nota);
                mostrar(nueva.get());
            }
        } catch (IllegalArgumentException e) {
            // una de las estaciones ya no existe
            ultimaConsulta = null;
            ultimaRuta = null;
            resultado.removeAll();
            diagrama.mostrarRuta(null);
            mensaje(e.getMessage());
        }
    }

    @Override
    protected void onAttach(AttachEvent evento) {
        UI ui = evento.getUI();
        cancelar = difusor.suscribir(aviso -> ui.access(() -> alCambiarRed(aviso)));
    }

    @Override
    protected void onDetach(DetachEvent evento) {
        if (cancelar != null) {
            cancelar.run();
            cancelar = null;
        }
    }
}
