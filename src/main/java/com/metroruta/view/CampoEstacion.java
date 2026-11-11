package com.metroruta.view;

import com.metroruta.service.BusquedaService;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

// campo de texto que sugiere estaciones mientras se escribe (usa el trie del servicio)
public class CampoEstacion extends VerticalLayout {

    private static final int MAXIMO_SUGERENCIAS = 8;

    private final transient BusquedaService busquedaService;
    private final TextField campo = new TextField();
    private final ListBox<String> sugerencias = new ListBox<>();
    private final Span mensaje = new Span();
    private String seleccion;
    private transient Consumer<String> alElegir;

    public CampoEstacion(BusquedaService busquedaService, String etiqueta) {
        this.busquedaService = busquedaService;
        setPadding(false);
        setSpacing(false);

        campo.setLabel(etiqueta);
        campo.setPlaceholder("Escribe el nombre de la estación");
        campo.setClearButtonVisible(true);
        campo.setWidthFull();
        addClassName("station-field");
        // EAGER: busca en cada letra que se escribe
        campo.setValueChangeMode(ValueChangeMode.EAGER);
        campo.addValueChangeListener(evento -> alEscribir(evento.getValue()));

        // isFromClient: ignora los cambios que hace el propio setItems
        sugerencias.addValueChangeListener(evento -> {
            if (evento.isFromClient() && evento.getValue() != null) {
                elegir(evento.getValue());
            }
        });
        sugerencias.setVisible(false);
        mensaje.setVisible(false);
        add(campo, sugerencias, mensaje);
    }

    // la estacion que se eligio de la lista (vacio si solo escribio texto)
    public Optional<String> getSeleccion() {
        return Optional.ofNullable(seleccion);
    }

    public void setAlElegir(Consumer<String> alElegir) {
        this.alElegir = alElegir;
    }

    private void alEscribir(String texto) {
        // si el texto es el de la estacion elegida, no se vuelve a buscar
        if (texto.equals(seleccion)) {
            ocultar();
            return;
        }
        seleccion = null;
        if (texto.isBlank()) {
            ocultar();
            return;
        }
        List<String> resultado = busquedaService.buscar(texto, MAXIMO_SUGERENCIAS);
        if (resultado.isEmpty()) {
            sugerencias.setVisible(false);
            mensaje.setText("No hay estaciones que empiecen con «" + texto.trim() + "»");
            mensaje.setVisible(true);
        } else {
            sugerencias.setItems(resultado);
            sugerencias.setVisible(true);
            mensaje.setVisible(false);
        }
    }

    private void elegir(String nombre) {
        seleccion = nombre;
        campo.setValue(nombre);
        ocultar();
        if (alElegir != null) {
            alElegir.accept(nombre);
        }
    }

    private void ocultar() {
        sugerencias.setVisible(false);
        mensaje.setVisible(false);
    }
}