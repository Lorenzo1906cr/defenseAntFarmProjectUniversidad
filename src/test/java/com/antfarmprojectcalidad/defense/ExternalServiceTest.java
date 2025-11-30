package com.antfarmprojectcalidad.defense.service;

import com.antfarmprojectcalidad.defense.model.Threat;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExternalServiceTest {

    private MockWebServer mockWebServer;
    private ExternalService externalService;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();

        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();

        externalService = new ExternalService(baseUrl, webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void getActiveThreats_returnsEmptyList_initially() {
        // No response from server: default empty response
        mockWebServer.enqueue(new MockResponse()
                .setBody("[]")
                .addHeader("Content-Type", "application/json"));

        List<Threat> result = externalService.getActiveThreats();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getActiveThreats_returnsMappedThreats() {
        String body = """
            [
                { "id": 1, "nombre": "Amenaza A", "estado": "activa" },
                { "id": 2, "nombre": "Amenaza B", "estado": "activa" }
            ]
            """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(body)
                .addHeader("Content-Type", "application/json"));

        List<Threat> result = externalService.getActiveThreats();

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals("Amenaza A", result.get(0).getNombre());
        assertEquals("activa", result.get(0).getEstado());
    }

    @Test
    void getActiveThreats_returnsEmptyList_whenServerError() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{\"error\": \"internal\"}")
                .addHeader("Content-Type", "application/json"));

        List<Threat> threats = externalService.getActiveThreats();

        assertNotNull(threats);
        assertTrue(threats.isEmpty());
    }

    @Test
    void getActiveThreats_returnsEmptyList_whenTimeout() {
        mockWebServer.enqueue(
                new MockResponse()
                        .setBody("[]")
                        .setBodyDelay(5, java.util.concurrent.TimeUnit.SECONDS)
        );

        List<Threat> threats = externalService.getActiveThreats();

        assertNotNull(threats);
        assertTrue(threats.isEmpty());
    }

    @Test
    void getActiveThreats_returnsEmptyList_whenInvalidJson() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{ invalid json }")
                .addHeader("Content-Type", "application/json"));

        List<Threat> threats = externalService.getActiveThreats();

        assertNotNull(threats);
        assertTrue(threats.isEmpty());
    }

    @Test
    void deactivateThreat_returnsTrue_onSuccess() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        boolean result = externalService.deactivateThreat(123);

        assertTrue(result);

        // Verify the request
        var recorded = mockWebServer.takeRequest();
        assertEquals("PUT", recorded.getMethod());
        assertEquals("/123", recorded.getPath());  // baseUrl ends with "/"
        assertEquals("{\"estado\":\"inactiva\"}", recorded.getBody().readUtf8());
    }

    @Test
    void deactivateThreat_returnsFalse_onServerError() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .addHeader("Content-Type", "application/json"));

        boolean result = externalService.deactivateThreat(999);

        assertFalse(result);
    }

}