package com.antfarmprojectcalidad.defense.scheduler;

import com.antfarmprojectcalidad.defense.model.MensajeRequest;
import com.antfarmprojectcalidad.defense.model.MensajeResponse;
import com.antfarmprojectcalidad.defense.model.Threat;
import com.antfarmprojectcalidad.defense.service.CommunicationService;
import com.antfarmprojectcalidad.defense.service.ExternalService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ServiceMonitor {
    public static final AtomicBoolean antFarmInDanger = new AtomicBoolean(false);
    public static final Map<Integer, Threat> threats = new ConcurrentHashMap<>();
    public static final Map<Integer, Threat> threatsWaitingForAnts = new ConcurrentHashMap<>();

    private final ExternalService externalService;
    private final CommunicationService communicationService;

    //for testing
    private List<String> processedTypes = new ArrayList<>();

    public ServiceMonitor(ExternalService externalService, CommunicationService communicationService) {
        this.externalService = externalService;
        this.communicationService = communicationService;
    }

    // Runs every 10 seconds
    @Scheduled(fixedRate = 10000)
    public void checkForUpdates() {
        System.out.println("Getting updates from external service");
        List<Threat> threatsNew = externalService.getActiveThreats();

        for (Threat threat : threatsNew) {
            if (!threats.containsKey(threat.getId())) {
                handleThreat(threat);
                threats.put(threat.getId(), threat);
            }
        }

        if (!threats.isEmpty()) {
            antFarmInDanger.set(true);
        }
    }

    // Runs every 10 seconds
    @Scheduled(fixedRate = 10000)
    public void checkIncomingMessages() {
        processedTypes.clear();

        List<MensajeResponse> mensajes = communicationService.obtenerMensaje("S05_DEF");
        if (mensajes == null || mensajes.isEmpty()) return;

        ObjectMapper mapper = new ObjectMapper();

        for (MensajeResponse msg : mensajes) {
            String json = msg.getMensaje();
            if (json == null || json.isBlank()) continue;

            try {
                Map<String, Object> root = mapper.readValue(json, Map.class);
                String tipo = (String) root.get("tipo");
                Map<String, Object> contenido = (Map<String, Object>) root.get("contenido");

                if (tipo == null) continue;

                if (tipo.equals("asignacion_hormigas")) {
                    processedTypes.add("asignacion_hormigas");
                    continue;
                }

                if (tipo.equals("rechazo_hormigas")) {
                    processedTypes.add("rechazo_hormigas");
                    continue;
                }

            } catch (Exception e) {
                // Invalid JSON is ignored
            }
        }
    }

    public void handleThreat(Threat threat) {
        System.out.println("Processing threat: " + threat.getId());

        MensajeResponse response = requestSupport(threat);
        if ("Mensaje creado con éxito".equalsIgnoreCase(response.getMensaje())) {
            threatsWaitingForAnts.put(threat.getId(), threat);
        }
    }

    public MensajeResponse requestSupport(Threat threat) {
        Map<String, Object> mesaje = new HashMap<>();
        mesaje.put("tipo", "solicitud_hormigas");

        Map<String, Object> contenido = new HashMap<>();
        contenido.put("request_ref", 1);
        contenido.put("threat_id", threat.getId());
        contenido.put("ants_needed", threat.getCosto_hormigas());

        mesaje.put("contenido", contenido);

        MensajeRequest request = new MensajeRequest();
        request.setEmisor("S05_DEF");
        request.setReceptor("S03_REI");
        request.setContenido(mesaje);

        MensajeResponse response = communicationService.enviarMensaje(request);
        System.out.println("ID recibido: " + response.getId());

        return response;
    }

    public List<String> getProcessedTypesForTest() { return processedTypes; }
}
