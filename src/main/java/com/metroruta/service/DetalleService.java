package com.metroruta.service;

import com.metroruta.dto.Ruta;
import com.metroruta.dto.Segmento;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

// parte una ruta en tramos, uno por cada linea que se usa
@Service
public class DetalleService {

    public List<Segmento> armar(Ruta ruta) {
        if (ruta == null || ruta.pasos().isEmpty()) {
            throw new IllegalArgumentException("No hay ruta para detallar");
        }
        List<Segmento> segmentos = new ArrayList<>();
        String linea = null;
        List<String> estaciones = new ArrayList<>();
        int inicio = 0;
        int ultimo = 0;
        int transbordo = 0;

        for (Ruta.Paso paso : ruta.pasos()) {
            if (linea == null) {
                linea = paso.linea();
                estaciones.add(paso.estacion());
                inicio = paso.minutosAcumulados();
            } else if (paso.transbordo()) {
                // el transbordo cierra el tramo anterior y abre uno en la otra linea
                segmentos.add(new Segmento(linea, estaciones, ultimo - inicio, transbordo));
                transbordo = paso.minutosAcumulados() - ultimo;
                linea = paso.linea();
                estaciones = new ArrayList<>();
                estaciones.add(paso.estacion());
                inicio = paso.minutosAcumulados();
            } else {
                estaciones.add(paso.estacion());
            }
            ultimo = paso.minutosAcumulados();
        }
        segmentos.add(new Segmento(linea, estaciones, ultimo - inicio, transbordo));
        return segmentos;
    }
}
