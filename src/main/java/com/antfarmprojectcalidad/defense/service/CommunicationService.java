package com.antfarmprojectcalidad.defense.service;

import com.antfarmprojectcalidad.defense.model.MensajeRequest;
import com.antfarmprojectcalidad.defense.model.MensajeResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class CommunicationService {
    private String commUrl;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CommunicationService(String commUrl) {
        this.webClient = WebClient.create();
        this.commUrl = commUrl;
    }

    public CommunicationService(String commUrl, WebClient webClient) {
        this.commUrl = commUrl;
        this.webClient = webClient;
    }

    public MensajeResponse enviarMensaje(MensajeRequest request) {
        try {
            return webClient.post()
                    .uri(commUrl)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            response -> Mono.error(new RuntimeException("API error"))
                    )
                    .bodyToMono(MensajeResponse.class)
                    .onErrorReturn(new MensajeResponse())  // return empty object on error
                    .block();
        } catch (Exception e) {
            return new MensajeResponse();
        }
    }

    public MensajeResponse obtenerMensaje(String receptor) {
        try {
            return webClient.get()
                    .uri(commUrl + "/api/mensaje?receptor=" + receptor)
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            response -> Mono.error(new RuntimeException("API error"))
                    )
                    .bodyToMono(MensajeResponse.class)
                    .onErrorReturn(new MensajeResponse())
                    .block();
        } catch (Exception e) {
            return new MensajeResponse();
        }
    }
}
