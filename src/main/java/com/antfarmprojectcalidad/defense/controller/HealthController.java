package com.antfarmprojectcalidad.defense.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {
    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of("status", "OK", "timestamp", LocalDateTime.now());
    }
}
