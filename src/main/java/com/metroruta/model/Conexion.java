package com.metroruta.model;

import jakarta.persistence.*;

// un tramo entre dos estaciones de una linea, con sus minutos
@Entity
@Table(name = "conexiones")
public class Conexion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Estacion origen;

    @ManyToOne(optional = false)
    private Estacion destino;

    @ManyToOne(optional = false)
    private Linea linea;

    @Column(nullable = false)
    private int minutos;

    protected Conexion() {
    }

    public Conexion(Estacion origen, Estacion destino, Linea linea, int minutos) {
        this.origen = origen;
        this.destino = destino;
        this.linea = linea;
        this.minutos = minutos;
    }

    public Long getId() { return id; }
    public Estacion getOrigen() { return origen; }
    public void setOrigen(Estacion origen) { this.origen = origen; }
    public Estacion getDestino() { return destino; }
    public void setDestino(Estacion destino) { this.destino = destino; }
    public Linea getLinea() { return linea; }
    public void setLinea(Linea linea) { this.linea = linea; }
    public int getMinutos() { return minutos; }
    public void setMinutos(int minutos) { this.minutos = minutos; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Conexion otra)) return false;
        return id != null && id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Conexion{" + origen.getNombre() + " -> " + destino.getNombre()
                + ", " + linea.getNombre() + ", " + minutos + " min}";
    }
}
