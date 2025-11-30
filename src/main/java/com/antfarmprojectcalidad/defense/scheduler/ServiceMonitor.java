package com.antfarmprojectcalidad.defense.scheduler;

import com.antfarmprojectcalidad.defense.model.MensajeRequest;
import com.antfarmprojectcalidad.defense.model.MensajeResponse;
import com.antfarmprojectcalidad.defense.model.Threat;
import com.antfarmprojectcalidad.defense.service.CommunicationService;
import com.antfarmprojectcalidad.defense.service.ExternalService;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ServiceMonitor {
    @Value("${monitor.minutes}")
    public Duration minutes;

    public static final AtomicBoolean antFarmInDanger = new AtomicBoolean(false);
    public static final Map<Integer, Threat> threats = new ConcurrentHashMap<>();
    public static final Map<Integer, Threat> threatsWaitingForAnts = new ConcurrentHashMap<>();
    public static final Map<Object, Object> threatRefDictionary = new ConcurrentHashMap<>();
    public static final Map<Integer, Instant> threatsDefending = new ConcurrentHashMap<>();
    public static final Map<Integer, Object> antsDefending = new ConcurrentHashMap<>();

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
                int randomNumber = ThreadLocalRandom.current().nextInt(100000, 1000000);
                String requestId = "req-" + randomNumber;

                handleThreat(threat, requestId);
                threats.put(threat.getId(), threat);
                threatRefDictionary.put(requestId, threat.getId());
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
                String requestId = contenido.get("request_ref").toString();

                if (tipo == null) continue;

                if (tipo.equals("asignacion_hormigas")) {
                    processedTypes.add("asignacion_hormigas");
                    List<Map<String, Object>> ants = (List<Map<String, Object>>) contenido.get("ants");
                    startDefense(requestId, ants);
                    continue;
                }

                if (tipo.equals("rechazo_hormigas")) {
                    processedTypes.add("rechazo_hormigas");
                    startDying(requestId);
                }

            } catch (Exception e) {
                // Invalid JSON is ignored
            }
        }
    }

    // Runs every 10 seconds
    @Scheduled(fixedRate = 10000)
    public void checkForDefenses() {
        for (Map.Entry<Integer, Instant> entry : threatsDefending.entrySet()) {
            Integer id = entry.getKey();
            Instant time = entry.getValue();

            if (time != null && time.isBefore(Instant.now().minus(minutes))) {
                Threat threat = threats.get(id);
                List<Map<String, Object>> ants = (List<Map<String, Object>>) antsDefending.get(id);
                List<Map<String, Object>> survAnts = new ArrayList<>();

                for (Map<String, Object> ant : ants) {
                    boolean randomBool = ThreadLocalRandom.current().nextBoolean();
                    if (randomBool) {
                        survAnts.add(ant);
                    }
                }

                externalService.deactivateThreat(threat.getId());
                returnAnts(threat, ants);
                onDefenseExpired(id, threat, survAnts);
            }
        }
    }

    private void startDefense(String requestId, List<Map<String, Object>> ants) {
        Integer threatId = (Integer) threatRefDictionary.get(requestId);
        threatsDefending.put(threatId, Instant.now());
        antsDefending.put(threatId, ants);
    }

    private void startDying(String requestId) {
        Integer threatId = (Integer) threatRefDictionary.get(requestId);
        informMuerteHormiguero(threatId);
    }

    public void handleThreat(Threat threat, String requestId) {
        System.out.println("Processing threat: " + threat.getId());

        MensajeResponse response = requestSupport(threat, requestId);
        if ("Mensaje creado con éxito".equalsIgnoreCase(response.getMensaje())) {
            threatsWaitingForAnts.put(threat.getId(), threat);
        }
    }

    public MensajeResponse requestSupport(Threat threat, String requestId) {
        Map<String, Object> mesaje = new HashMap<>();
        mesaje.put("tipo", "solicitud_hormigas");

        Map<String, Object> contenido = new HashMap<>();
        contenido.put("request_ref", requestId);
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

    public MensajeResponse returnAnts(Threat threat, List<Map<String, Object>> ants) {
        Map<String, Object> mesaje = new HashMap<>();
        mesaje.put("tipo", "resultado_ataque");

        Map<String, Object> contenido = new HashMap<>();
        contenido.put("threat_id", threat.getId());
        contenido.put("survivors", threat.getCosto_hormigas());

        mesaje.put("contenido", contenido);

        MensajeRequest request = new MensajeRequest();
        request.setEmisor("S05_DEF");
        request.setReceptor("S03_REI");
        request.setContenido(mesaje);

        MensajeResponse response = communicationService.enviarMensaje(request);
        System.out.println("ID recibido: " + response.getId());

        return response;
    }

    public MensajeResponse informMuerteHormiguero(Integer threat) {
        Map<String, Object> mesaje = new HashMap<>();
        mesaje.put("tipo", "fin_hormiguero");

        Map<String, Object> contenido = new HashMap<>();
        contenido.put("threat_id", threat);

        mesaje.put("contenido", contenido);

        MensajeRequest request = new MensajeRequest();
        request.setEmisor("S05_DEF");
        request.setReceptor("S03_REI");
        request.setContenido(mesaje);

        MensajeResponse response = communicationService.enviarMensaje(request);
        System.out.println("ID recibido: " + response.getId());

        return response;
    }

    protected void onDefenseExpired(Integer threatId, Threat threat, List<Map<String, Object>> ants) {
    }
    public List<String> getProcessedTypesForTest() { return processedTypes; }
}
