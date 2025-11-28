package com.antfarmprojectcalidad.defense.service;

import com.antfarmprojectcalidad.defense.model.Threat;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
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
        try {
            return webClient.get()
                    .uri("/threats/active")
                    .retrieve()
                    .onStatus(
                            status -> status.isError(),
                            response -> Mono.error(new RuntimeException("API error"))
                    )
                    .bodyToFlux(Threat.class)
                    .collectList()
                    .onErrorReturn(Collections.emptyList())
                    .block();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
