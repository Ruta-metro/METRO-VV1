package com.metroruta.config;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.dependency.StyleSheet;

// Push deja que el servidor le mande cambios a las pantallas abiertas sin que el usuario haga nada
@StyleSheet("styles/metro.css")
@Push
public class AplicacionVaadin implements AppShellConfigurator {
}
