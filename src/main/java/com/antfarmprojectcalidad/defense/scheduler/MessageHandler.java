package com.antfarmprojectcalidad.defense.scheduler;

import java.util.Map;

public interface MessageHandler {
    void handleAsignacion(Map<String, Object> contenido);
    void handleRechazo(Map<String, Object> contenido);
    void handleUnknown(String tipo, Map<String,Object> contenido);
}
