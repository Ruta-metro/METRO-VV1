package com.metroruta.config;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;

// @Push deja que el servidor le mande cambios a las pantallas abiertas sin que el usuario haga nada
@Push
public class AplicacionVaadin implements AppShellConfigurator {
}
