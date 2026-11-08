package com.metroruta.view;

import com.metroruta.model.Estacion;
import com.metroruta.service.EstacionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

// el admin crea, edita y elimina estaciones
@Route("admin/estaciones")
@PageTitle("Estaciones | MetroRuta")
@RolesAllowed("ADMIN")
public class VistaEstaciones extends VerticalLayout {

    private final transient EstacionService estacionService;

    private final TextField nombre = new TextField("Nombre");
    private final IntegerField x = new IntegerField("Posición X en el diagrama (opcional)");
    private final IntegerField y = new IntegerField("Posición Y en el diagrama (opcional)");
    private final Grid<Estacion> tabla = new Grid<>();
    private final Button guardar = new Button("Guardar cambios");
    private final Button eliminar = new Button("Eliminar");
    private transient Estacion seleccionada;

    public VistaEstaciones(EstacionService estacionService) {
        this.estacionService = estacionService;

        Button crear = new Button("Crear estación", evento -> crear());
        guardar.addClickListener(evento -> guardarCambios());
        eliminar.addClickListener(evento -> confirmarEliminar());
        Button limpiar = new Button("Limpiar", evento -> tabla.deselectAll());

        tabla.addColumn(Estacion::getNombre).setHeader("Estación");
        tabla.addColumn(e -> e.isCerrada() ? "Cerrada" : "Abierta").setHeader("Estado");
        tabla.addColumn(Estacion::getPosX).setHeader("X");
        tabla.addColumn(Estacion::getPosY).setHeader("Y");
        tabla.asSingleSelect().addValueChangeListener(evento -> elegir(evento.getValue()));

        add(new H2("Administrar estaciones"), new HorizontalLayout(nombre, x, y),
                new HorizontalLayout(crear, guardar, eliminar, limpiar), tabla);
        elegir(null);
        refrescar();
    }

    private void elegir(Estacion estacion) {
        seleccionada = estacion;
        nombre.setValue(estacion == null ? "" : estacion.getNombre());
        x.setValue(estacion == null ? null : estacion.getPosX());
        y.setValue(estacion == null ? null : estacion.getPosY());
        guardar.setEnabled(estacion != null);
        eliminar.setEnabled(estacion != null);
    }

    private void crear() {
        ejecutar(() -> estacionService.crear(nombre.getValue(), x.getValue(), y.getValue()), "Estación creada");
    }

    private void guardarCambios() {
        ejecutar(() -> estacionService.editar(seleccionada.getNombre(), nombre.getValue(), x.getValue(), y.getValue()),
                "Cambios guardados");
    }

    private void confirmarEliminar() {
        ConfirmDialog dialogo = new ConfirmDialog();
        dialogo.setHeader("Eliminar estación");
        dialogo.setText("Se borrarán también sus conexiones. ¿Seguro?");
        dialogo.setCancelable(true);
        dialogo.setConfirmText("Eliminar");
        dialogo.setCancelText("Cancelar");
        dialogo.addConfirmListener(evento -> ejecutar(() -> estacionService.eliminar(seleccionada.getNombre()),
                "Estación eliminada"));
        dialogo.open();
    }

    private void ejecutar(Runnable accion, String textoOk) {
        try {
            accion.run();
            Notification.show(textoOk);
            tabla.deselectAll();
            elegir(null);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Notification.show(e.getMessage());
        }
        refrescar();
    }

    private void refrescar() {
        tabla.setItems(estacionService.listar());
    }
}
