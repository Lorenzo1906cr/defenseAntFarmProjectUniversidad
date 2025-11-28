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
        List<Threat> threats = externalService.getActiveThreats();

        for (Threat threat : threats) {
            System.out.println(threat.getId());
        }
    }
}
