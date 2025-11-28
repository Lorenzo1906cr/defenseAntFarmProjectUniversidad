package com.antfarmprojectcalidad.defense.service;

import com.antfarmprojectcalidad.defense.model.Threat;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Service
public class ExternalService {
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
                    .uri(entornoUrl)
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
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
