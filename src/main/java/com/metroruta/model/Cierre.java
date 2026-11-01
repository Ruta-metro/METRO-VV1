package com.metroruta.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// registro de cada cierre o reapertura de una estacion
@Entity
@Table(name = "cierres")
public class Cierre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Estacion estacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccionCierre accion;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    private String usuario;

    protected Cierre() {
    }

    public Cierre(Estacion estacion, AccionCierre accion, String usuario) {
        this.estacion = estacion;
        this.accion = accion;
        this.usuario = usuario;
        this.fecha = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Estacion getEstacion() { return estacion; }
    public AccionCierre getAccion() { return accion; }
    public LocalDateTime getFecha() { return fecha; }
    public String getUsuario() { return usuario; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cierre otro)) return false;
        return id != null && id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Cierre{" + estacion.getNombre() + ", " + accion + ", " + fecha + "}";
    }
}
