package com.antfarmprojectcalidad.defense.controller;

import com.antfarmprojectcalidad.defense.scheduler.ServiceMonitor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {
    @GetMapping("/status")
    public Map<String, Object> status() {
        Map<String, Object> response = new HashMap<>();
        response.put("farm_attacked", ServiceMonitor.antFarmInDanger.get());
        response.put("threats", ServiceMonitor.threats);
        response.put("threats_defending", ServiceMonitor.threatsDefending);
        return response;
    }
}
