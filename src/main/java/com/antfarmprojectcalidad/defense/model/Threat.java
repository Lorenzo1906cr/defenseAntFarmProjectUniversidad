package com.antfarmprojectcalidad.defense.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Threat {
    private int id;
    private String estado;
    private String nombre;
    private String costo_hormigas;

    public Threat() {}

    public Threat(int id, String nombre, String estado) {
        this.id = id;
        this.estado = estado;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCosto_hormigas() {
        return costo_hormigas;
    }

    public void setCosto_hormigas(String costo_hormigas) {
        this.costo_hormigas = costo_hormigas;
    }
}
