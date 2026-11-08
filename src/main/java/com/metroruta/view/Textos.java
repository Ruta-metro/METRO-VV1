package com.metroruta.view;

import com.metroruta.model.CriterioRuta;

// textos que se repiten en varias vistas
public final class Textos {

    private Textos() {
    }

    public static String criterio(CriterioRuta criterio) {
        return criterio == CriterioRuta.RAPIDA ? "Más rápida" : "Menos transbordos";
    }
}
