package com.metroruta.view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

// autoLayout false: el login no lleva el menu de arriba
@Route(value = "login", autoLayout = false)
@PageTitle("Ingresar | MetroRuta")
@AnonymousAllowed
public class VistaLogin extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm formulario = new LoginForm();

    public VistaLogin() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        // el formulario manda los datos a spring security
        formulario.setAction("login");
        formulario.setI18n(textosEnEspanol());
        formulario.setForgotPasswordButtonVisible(false);
        add(new H1("MetroRuta Medellín"), formulario);
    }

    private LoginI18n textosEnEspanol() {
        LoginI18n textos = LoginI18n.createDefault();
        textos.getForm().setTitle("Ingresar");
        textos.getForm().setUsername("Usuario");
        textos.getForm().setPassword("Contraseña");
        textos.getForm().setSubmit("Entrar");
        textos.getErrorMessage().setTitle("Datos incorrectos");
        textos.getErrorMessage().setMessage("Revisa tu usuario y tu contraseña e intenta de nuevo.");
        return textos;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent evento) {
        // spring security agrega ?error cuando las credenciales fallan
        if (evento.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            formulario.setError(true);
        }
    }
}