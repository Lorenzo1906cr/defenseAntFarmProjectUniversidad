package com.antfarmprojectcalidad.defense.model;

import java.util.Map;

public class MensajeRequest {
    private String emisor;
    private String receptor;
    private Map<String, Object> contenido;

    public String getEmisor() {
        return emisor;
    }

    public void setEmisor(String emisor) {
        this.emisor = emisor;
    }

    public String getReceptor() {
        return receptor;
    }

    public void setReceptor(String receptor) {
        this.receptor = receptor;
    }

    public Map<String, Object> getContenido() {
        return contenido;
    }

    public void setContenido(Map<String, Object> contenido) {
        this.contenido = contenido;
    }
}
