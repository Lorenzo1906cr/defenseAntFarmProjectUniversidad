package com.antfarmprojectcalidad.defense.scheduler;

import com.antfarmprojectcalidad.defense.service.ExternalService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ServiceMonitor {
    private final ExternalService externalService;

    public ServiceMonitor(ExternalService externalService) {
        this.externalService = externalService;
    }

    // Runs every 10 seconds
    @Scheduled(fixedRate = 10000)
    public void checkForUpdates() {
        boolean changed = externalService.hasChanged();

        if (changed) {
            System.out.println("Change detected!");
            // Do something: update cache, send event, log, etc.
        }
    }
}
