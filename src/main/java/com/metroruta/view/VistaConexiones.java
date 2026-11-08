package com.metroruta.view;

import com.metroruta.model.Conexion;
import com.metroruta.service.ConexionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

// el admin crea, cambia los minutos y elimina tramos entre estaciones
@Route("admin/conexiones")
@PageTitle("Conexiones | MetroRuta")
@RolesAllowed("ADMIN")
public class VistaConexiones extends VerticalLayout {

    private final transient ConexionService conexionService;

    private final ComboBox<String> origen = new ComboBox<>("Origen");
    private final ComboBox<String> destino = new ComboBox<>("Destino");
    private final ComboBox<String> linea = new ComboBox<>("Línea");
    private final IntegerField minutos = new IntegerField("Minutos");
    private final Grid<Conexion> tabla = new Grid<>();
    private final Button cambiar = new Button("Cambiar minutos");
    private final Button eliminar = new Button("Eliminar tramo");
    private transient Conexion seleccionada;

    public VistaConexiones(ConexionService conexionService) {
        this.conexionService = conexionService;

        Button crear = new Button("Crear tramo", evento -> crear());
        cambiar.addClickListener(evento -> cambiarMinutos());
        eliminar.addClickListener(evento -> confirmarEliminar());

        tabla.addColumn(c -> c.getLinea().getNombre()).setHeader("Línea");
        tabla.addColumn(c -> c.getOrigen().getNombre()).setHeader("Origen");
        tabla.addColumn(c -> c.getDestino().getNombre()).setHeader("Destino");
        tabla.addColumn(Conexion::getMinutos).setHeader("Minutos");
        tabla.asSingleSelect().addValueChangeListener(evento -> elegir(evento.getValue()));

        add(new H2("Administrar conexiones"), new HorizontalLayout(origen, destino, linea, minutos),
                new HorizontalLayout(crear, cambiar, eliminar), tabla);
        elegir(null);
        refrescar();
    }

    private void elegir(Conexion conexion) {
        seleccionada = conexion;
        cambiar.setEnabled(conexion != null);
        eliminar.setEnabled(conexion != null);
        if (conexion != null) {
            minutos.setValue(conexion.getMinutos());
        }
    }

    private void crear() {
        if (minutos.getValue() == null) {
            Notification.show("Escribe los minutos del tramo");
            return;
        }
        ejecutar(() -> conexionService.crear(origen.getValue(), destino.getValue(), linea.getValue(),
                minutos.getValue()), "Tramo creado");
    }

    private void cambiarMinutos() {
        if (minutos.getValue() == null) {
            Notification.show("Escribe los minutos del tramo");
            return;
        }
        ejecutar(() -> conexionService.cambiarMinutos(seleccionada.getId(), minutos.getValue()), "Minutos cambiados");
    }

    private void confirmarEliminar() {
        ConfirmDialog dialogo = new ConfirmDialog();
        dialogo.setHeader("Eliminar tramo");
        dialogo.setText("¿Seguro que quieres eliminar este tramo?");
        dialogo.setCancelable(true);
        dialogo.setConfirmText("Eliminar");
        dialogo.setCancelText("Cancelar");
        dialogo.addConfirmListener(evento -> ejecutar(() -> conexionService.eliminar(seleccionada.getId()),
                "Tramo eliminado"));
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
        origen.setItems(conexionService.nombresDeEstaciones());
        destino.setItems(conexionService.nombresDeEstaciones());
        linea.setItems(conexionService.nombresDeLineas());
        tabla.setItems(conexionService.listar());
    }
}
