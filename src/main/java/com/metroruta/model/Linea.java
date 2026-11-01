package com.metroruta.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "lineas")
public class Linea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    // color para dibujarla en el diagrama, ej: #00a651
    @Column(nullable = false)
    private String color;

    protected Linea() {
    }

    public Linea(String nombre, String color) {
        this.nombre = nombre;
        this.color = color;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Linea otra)) return false;
        return Objects.equals(nombre, otra.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre);
    }

    @Override
    public String toString() {
        return "Linea{nombre=" + nombre + ", color=" + color + "}";
    }
}
