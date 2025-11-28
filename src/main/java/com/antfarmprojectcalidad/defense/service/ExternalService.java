package com.antfarmprojectcalidad.defense.service;

import com.antfarmprojectcalidad.defense.model.Threat;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Objects;

@Service
public class ExternalService {
    private String lastValue = null;
    private String entornoUrl;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExternalService(String entornoUrl) {
        this.webClient = WebClient.create();
        this.entornoUrl = entornoUrl;
    }

    public ExternalService(String entornoUrl, WebClient webClient) {
        this.entornoUrl = entornoUrl;
        this.webClient = webClient;
    }

    public List<Threat> getActiveThreats() {
        return webClient.get()
                .uri(entornoUrl + "/threats")
                .retrieve()
                .bodyToFlux(Threat.class)
                .collectList()
                .block();
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
