package com.metroruta.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.metroruta.model.CriterioRuta;
import com.metroruta.model.Estacion;
import com.metroruta.repository.EstacionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DiagramaServiceTest {

    @Autowired
    private DiagramaService diagramaService;

    @Autowired
    private RutaService rutaService;

    @Autowired
    private EstacionRepository estacionRepository;

    @AfterEach
    void abrirTodo() {
        for (Estacion estacion : estacionRepository.findAll()) {
            if (estacion.isCerrada()) {
                estacion.setCerrada(false);
                estacionRepository.save(estacion);
            }
        }
    }

    private int cantidad(String texto, String buscado) {
        int total = 0;
        for (int i = texto.indexOf(buscado); i >= 0; i = texto.indexOf(buscado, i + 1)) {
            total++;
        }
        return total;
    }

    @Test
    void todasLasEstacionesTienenPosicionYSeDibujan() {
        diagramaService.asegurarPosiciones();
        for (Estacion estacion : estacionRepository.findAll()) {
            assertTrue(estacion.getPosX() != null && estacion.getPosY() != null, "sin posicion: " + estacion);
        }
        String svg = diagramaService.generarSvg(null);
        assertTrue(svg.startsWith("<svg"));
        assertTrue(svg.endsWith("</svg>"));
        assertTrue(svg.contains("Niquía"));
        assertTrue(svg.contains("Santo Domingo Savio"));
        assertEquals(estacionRepository.count(), cantidad(svg, "class=\"estacion\"") + cantidad(svg, "class=\"estacion-cerrada\""));
    }

    @Test
    void asegurarPosicionesNoCambiaLoQueYaTienePosicion() {
        diagramaService.asegurarPosiciones();
        assertEquals(0, diagramaService.asegurarPosiciones());
    }

    @Test
    void sinRutaNoHayTramosResaltados() {
        assertEquals(0, cantidad(diagramaService.generarSvg(null), "class=\"tramo-ruta\""));
    }

    @Test
    void laRutaResaltaUnTramoPorCadaSaltoEntreEstaciones() {
        // Prado a Poblado son 6 tramos de la linea A
        var ruta = rutaService.calcular("Prado", "Poblado", CriterioRuta.RAPIDA).orElseThrow();
        assertEquals(6, cantidad(diagramaService.generarSvg(ruta), "class=\"tramo-ruta\""));
    }

    @Test
    void elTransbordoNoAgregaUnTramoResaltado() {
        // Cisneros a Alpujarra: un tramo de la B, el transbordo y un tramo de la A
        var ruta = rutaService.calcular("Cisneros", "Alpujarra", CriterioRuta.RAPIDA).orElseThrow();
        assertEquals(2, cantidad(diagramaService.generarSvg(ruta), "class=\"tramo-ruta\""));
    }

    @Test
    void unaEstacionCerradaSeMarcaEnRojo() {
        String antes = diagramaService.generarSvg(null);
        assertFalse(antes.contains("estacion-cerrada"));
        Estacion prado = estacionRepository.findByNombreIgnoreCase("Prado").orElseThrow();
        prado.setCerrada(true);
        estacionRepository.save(prado);
        assertEquals(1, cantidad(diagramaService.generarSvg(null), "class=\"estacion-cerrada\""));
    }

    @Test
    void losNombresConSimbolosSeEscapan() {
        assertEquals("A &amp; B &lt;1&gt; &quot;x&quot;", DiagramaService.escapar("A & B <1> \"x\""));
    }
}
