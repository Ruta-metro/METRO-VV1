package com.metroruta.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// una ruta que un usuario consulto
@Entity
@Table(name = "historial_rutas")
public class HistorialRuta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    private String origen;

    @Column(nullable = false)
    private String destino;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CriterioRuta criterio;

    private int minutos;
    private int transbordos;

    protected HistorialRuta() {
    }

    public HistorialRuta(Usuario usuario, String origen, String destino,
                         CriterioRuta criterio, int minutos, int transbordos) {
        this.usuario = usuario;
        this.origen = origen;
        this.destino = destino;
        this.criterio = criterio;
        this.minutos = minutos;
        this.transbordos = transbordos;
        this.fecha = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public LocalDateTime getFecha() { return fecha; }
    public String getOrigen() { return origen; }
    public String getDestino() { return destino; }
    public CriterioRuta getCriterio() { return criterio; }
    public int getMinutos() { return minutos; }
    public int getTransbordos() { return transbordos; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HistorialRuta otro)) return false;
        return id != null && id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "HistorialRuta{" + origen + " -> " + destino + ", " + criterio + ", " + minutos + " min}";
    }
}
