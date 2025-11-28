package com.antfarmprojectcalidad.defense.service;

import com.antfarmprojectcalidad.defense.model.Threat;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;

@Service
public class ExternalService {
    private String lastValue = null;
    private final String baseUrl;

    public ExternalService(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public List<Threat> getActiveThreats() {
        return List.of();
    }

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
