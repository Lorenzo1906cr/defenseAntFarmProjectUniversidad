package com.antfarmprojectcalidad.defense.service;

import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ExternalService {
    private String lastValue = null;

    public boolean hasChanged() {
        // Example: call external API
        String newValue = callExternalService();

        boolean changed = !Objects.equals(newValue, lastValue);
        lastValue = newValue;
        return changed;
    }

    private String callExternalService() {
        // TODO: use WebClient, HttpURLConnection, or RestTemplate
        return "example";
    }
}
