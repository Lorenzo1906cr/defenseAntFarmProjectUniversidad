package com.antfarmprojectcalidad.defense.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Threat {

    private int id;

    @JsonProperty("zona_id")
    private int zonaId;

    private String nombre;
    private String tipo;

    @JsonProperty("costo_hormigas")
    private int costoHormigas;

    private String estado;

    @JsonProperty("hora_deteccion")
    private String horaDeteccion;

    @JsonProperty("hora_resolucion")
    private String horaResolucion;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getZonaId() { return zonaId; }
    public void setZonaId(int zonaId) { this.zonaId = zonaId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public int getCostoHormigas() { return costoHormigas; }
    public void setCostoHormigas(int costoHormigas) { this.costoHormigas = costoHormigas; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getHoraDeteccion() { return horaDeteccion; }
    public void setHoraDeteccion(String horaDeteccion) { this.horaDeteccion = horaDeteccion; }

    public String getHoraResolucion() { return horaResolucion; }
    public void setHoraResolucion(String horaResolucion) { this.horaResolucion = horaResolucion; }
}
