package com.antfarmprojectcalidad.defense.scheduler;

import com.antfarmprojectcalidad.defense.model.Threat;
import com.antfarmprojectcalidad.defense.service.ExternalService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ServiceMonitor {
    private final ExternalService externalService;

    public ServiceMonitor(ExternalService externalService) {
        this.externalService = externalService;
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
    }
}
