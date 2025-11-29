package com.antfarmprojectcalidad.defense;

import com.antfarmprojectcalidad.defense.model.MensajeRequest;
import com.antfarmprojectcalidad.defense.model.MensajeResponse;
import com.antfarmprojectcalidad.defense.service.CommunicationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;

public class CommunicationServiceTest {

    private WebClient webClient;
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    private WebClient.RequestBodySpec requestBodySpec;
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    private WebClient.ResponseSpec responseSpec;

    private CommunicationService communicationService;

    private final String URL = "http://localhost:8080/api/mensaje";

    @BeforeEach
    void setup() {
        webClient = mock(WebClient.class);
        requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        requestBodySpec = mock(WebClient.RequestBodySpec.class);
        requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(URL)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        MensajeResponse mockResponse = new MensajeResponse();
        mockResponse.setId("msg-001");
        mockResponse.setMensaje("Mensaje creado con éxito");
        mockResponse.setTimestamp("2025-10-08T17:20:00Z");
        mockResponse.setTtl(60000);

        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(MensajeResponse.class)).thenReturn(Mono.just(mockResponse));

        communicationService = new CommunicationService(URL, webClient);
    }

    @Test
    void testEnviarMensaje() {
        MensajeRequest request = new MensajeRequest();
        request.setEmisor("hormiga_3");
        request.setReceptor("hormiga_7");
        request.setContenido(Collections.emptyMap());

        MensajeResponse response = communicationService.enviarMensaje(request);

        assertNotNull(response);
        assertEquals("msg-001", response.getId());
        assertEquals("Mensaje creado con éxito", response.getMensaje());
        assertEquals(60000, response.getTtl());

        verify(webClient).post();
        verify(requestBodyUriSpec).uri(URL);
        verify(requestBodySpec).bodyValue(request);
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(MensajeResponse.class);
    }

    @Test
    void testObtenerMensaje() {
        String receptor = "S05_DEF";
        String expectedUrl = URL + "/api/mensaje?receptor=" + receptor;

        WebClient.RequestHeadersUriSpec getSpec = mock(WebClient.RequestHeadersUriSpec.class);
        when(webClient.get()).thenReturn(getSpec);
        when(getSpec.uri(expectedUrl)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

        MensajeResponse mockResponse = new MensajeResponse();
        mockResponse.setMensaje("asignacion_hormigas");
        mockResponse.setTimestamp("2025-10-08T17:20:00Z");
        mockResponse.setTtl(60000);

        when(responseSpec.bodyToMono(MensajeResponse.class)).thenReturn(Mono.just(mockResponse));

        MensajeResponse response = communicationService.obtenerMensaje(receptor);

        assertNotNull(response);
        assertEquals("asignacion_hormigas", response.getMensaje());

        verify(webClient).get();
        verify(getSpec).uri(expectedUrl);
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(MensajeResponse.class);
    }
}
