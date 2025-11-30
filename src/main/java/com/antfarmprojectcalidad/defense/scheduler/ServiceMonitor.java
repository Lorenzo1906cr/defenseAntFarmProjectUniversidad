package com.antfarmprojectcalidad.defense.scheduler;

import com.antfarmprojectcalidad.defense.model.MensajeRequest;
import com.antfarmprojectcalidad.defense.model.MensajeResponse;
import com.antfarmprojectcalidad.defense.model.Threat;
import com.antfarmprojectcalidad.defense.service.CommunicationService;
import com.antfarmprojectcalidad.defense.service.ExternalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ServiceMonitor {

    private static final Logger log = LoggerFactory.getLogger(ServiceMonitor.class);

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

    private List<String> processedTypes = new ArrayList<>();

    public ServiceMonitor(ExternalService externalService,
                          CommunicationService communicationService) {
        this.externalService = externalService;
        this.communicationService = communicationService;
    }

    // Runs every 10 seconds
    @Scheduled(fixedRate = 10000)
    public void checkForUpdates() {
        log.info("Checking for new threats...");

        List<Threat> threatsNew = externalService.getActiveThreats();
        log.debug("Active threats received: {}", threatsNew.size());

        for (Threat threat : threatsNew) {
            if (!threats.containsKey(threat.getId())) {

                int randomNumber = ThreadLocalRandom.current().nextInt(100000, 1000000);
                String requestId = "req-" + randomNumber;

                log.info("New threat detected: {} (requestId={})", threat.getId(), requestId);

                handleThreat(threat, requestId);
                threats.put(threat.getId(), threat);
                threatRefDictionary.put(requestId, threat.getId());
            }
        }

        if (!threats.isEmpty()) {
            antFarmInDanger.set(true);
            log.warn("Ant farm is in danger! {} active threats detected.", threats.size());
        }
    }

    // Runs every 10 seconds
    @Scheduled(fixedRate = 10000)
    public void checkIncomingMessages() {
        processedTypes.clear();
        log.info("Checking incoming messages...");

        List<MensajeResponse> mensajes = communicationService.obtenerMensaje("S05_DEF");

        if (mensajes == null || mensajes.isEmpty()) {
            log.debug("No incoming messages.");
            return;
        }

        ObjectMapper mapper = new ObjectMapper();

        for (MensajeResponse msg : mensajes) {
            String json = msg.getMensaje();
            if (json == null || json.isBlank()) {
                log.warn("Received empty or null message, skipping.");
                continue;
            }

            try {
                Map<String, Object> root = mapper.readValue(json, Map.class);
                String tipo = (String) root.get("tipo");
                Map<String, Object> contenido = (Map<String, Object>) root.get("contenido");
                String requestId = contenido.get("request_ref").toString();

                log.info("Processing message type={} for requestId={}", tipo, requestId);

                if (tipo == null) continue;

                if (tipo.equals("asignacion_hormigas")) {
                    processedTypes.add("asignacion_hormigas");

                    List<Map<String, Object>> ants = (List<Map<String, Object>>) contenido.get("ants");
                    startDefense(requestId, ants);

                    log.info("Ants assigned successfully for requestId={}", requestId);
                    continue;
                }

                if (tipo.equals("rechazo_hormigas")) {
                    processedTypes.add("rechazo_hormigas");

                    log.warn("Ant assignment rejected for requestId={}", requestId);
                    startDying(requestId);
                }

            } catch (Exception e) {
                log.error("Invalid JSON received, skipping. Raw message: {}", json, e);
            }
        }
    }

    // Runs every 10 seconds
    @Scheduled(fixedRate = 10000)
    public void checkForDefenses() {
        log.info("Checking active defenses...");

        for (Map.Entry<Integer, Instant> entry : threatsDefending.entrySet()) {
            Integer id = entry.getKey();
            Instant time = entry.getValue();

            if (time != null && time.isBefore(Instant.now().minus(minutes))) {
                log.warn("Defense expired for threat {}", id);

                Threat threat = threats.get(id);
                List<Map<String, Object>> ants = (List<Map<String, Object>>) antsDefending.get(id);
                List<Map<String, Object>> survAnts = new ArrayList<>();

                for (Map<String, Object> ant : ants) {
                    if (ThreadLocalRandom.current().nextBoolean()) {
                        survAnts.add(ant);
                    }
                }

                externalService.deactivateThreat(threat.getId());
                log.info("Threat {} deactivated in external service.", threat.getId());

                returnAnts(threat, ants);
                onDefenseExpired(id, threat, survAnts);
            }
        }
    }

    private void startDefense(String requestId, List<Map<String, Object>> ants) {
        Integer threatId = (Integer) threatRefDictionary.get(requestId);
        threatsDefending.put(threatId, Instant.now());
        antsDefending.put(threatId, ants);

        log.info("Started defense for threat {} with {} ants.", threatId, ants.size());
    }

    private void startDying(String requestId) {
        Integer threatId = (Integer) threatRefDictionary.get(requestId);
        log.warn("Threat {} entered dying state.", threatId);
        informMuerteHormiguero(threatId);
    }

    public void handleThreat(Threat threat, String requestId) {
        log.debug("Handling threat {} with requestId={}", threat.getId(), requestId);

        MensajeResponse response = requestSupport(threat, requestId);

        if ("Mensaje creado con éxito".equalsIgnoreCase(response.getMensaje())) {
            threatsWaitingForAnts.put(threat.getId(), threat);
            log.info("Threat {} added to waiting-for-ants list.", threat.getId());
        } else {
            log.error("Failed to create request message for threat {}", threat.getId());
        }
    }

    public MensajeResponse requestSupport(Threat threat, String requestId) {
        log.info("Requesting support for threat {} (ants_needed={})", threat.getId(), threat.getCosto_hormigas());

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

        log.debug("Support request sent. ResponseId={}", response.getId());
        return response;
    }

    public MensajeResponse returnAnts(Threat threat, List<Map<String, Object>> ants) {
        log.info("Returning ants for threat {}. Total sent={}", threat.getId(), ants.size());

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

        log.debug("Returned ants message sent. ResponseId={}", response.getId());
        return response;
    }

    public MensajeResponse informMuerteHormiguero(Integer threat) {
        log.warn("Informing ant colony death for threat {}", threat);

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

        log.debug("Death notification sent. ResponseId={}", response.getId());
        return response;
    }

    protected void onDefenseExpired(Integer threatId, Threat threat, List<Map<String, Object>> ants) {
        log.info("Custom handler: defense expired for threat {}. Survivors={}", threatId, ants.size());
    }

    public List<String> getProcessedTypesForTest() {
        return processedTypes;
    }
}