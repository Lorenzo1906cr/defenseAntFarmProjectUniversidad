package com.antfarmprojectcalidad.defense.scheduler;

import com.antfarmprojectcalidad.defense.model.MensajeRequest;
import com.antfarmprojectcalidad.defense.model.MensajeResponse;
import com.antfarmprojectcalidad.defense.model.Threat;
import com.antfarmprojectcalidad.defense.service.CommunicationService;
import com.antfarmprojectcalidad.defense.service.ExternalService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ServiceMonitor {
    private final ExternalService externalService;
    private final CommunicationService communicationService;

    public ServiceMonitor(ExternalService externalService, CommunicationService communicationService) {
        this.externalService = externalService;
        this.communicationService = communicationService;
    }

    // Runs every 10 seconds
    @Scheduled(fixedRate = 10000)
    public void checkForUpdates() {
        System.out.println("Getting updates from external service");
        List<Threat> threats = externalService.getActiveThreats();

        for (Threat threat : threats) {
            System.out.println("Processing threat outside: " + threat.getId());
            Thread thread = createThread(threat);
            thread.start();
        }
    }

    public Thread createThread(Threat threat) {
        return new Thread(() -> handleThreat(threat));
    }

    public void handleThreat(Threat threat) {
        System.out.println("Processing threat: " + threat.getId());



        //if ("Mensaje creado con éxito".equalsIgnoreCase(response.getMensaje())) {

        //}
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
}
