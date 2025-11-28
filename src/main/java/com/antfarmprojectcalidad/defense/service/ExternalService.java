package com.antfarmprojectcalidad.defense.service;

import com.antfarmprojectcalidad.defense.model.Threat;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Objects;

@Service
public class ExternalService {
    private String lastValue = null;
    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExternalService(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newHttpClient();
    }

    public List<Threat> getActiveThreats() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "threats"))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            return objectMapper.readValue(
                    response.body(),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to call external API", e);
        }
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
