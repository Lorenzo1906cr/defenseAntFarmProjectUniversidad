package com.antfarmprojectcalidad.defense;

import com.antfarmprojectcalidad.defense.model.Threat;
import com.antfarmprojectcalidad.defense.service.ExternalService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ExternalServiceTest {
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

        externalService = new ExternalService(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void getActiveThreats_returnsOnlyActiva() {
        String json = """
        [
          {
            "id": 200,
            "zona_id": 1,
            "nombre": "Amenaza Persistente 1",
            "tipo": "AGUILA",
            "costo_hormigas": 8,
            "estado": "activa",
            "hora_deteccion": "2025-11-19T09:10:00",
            "hora_resolucion": null
          },
          {
            "id": 201,
            "zona_id": 2,
            "nombre": "Amenaza Inactiva",
            "tipo": "SERPIENTE",
            "costo_hormigas": 12,
            "estado": "resuelta",
            "hora_deteccion": "2025-11-19T09:15:00",
            "hora_resolucion": "2025-11-19T10:15:00"
          },
          {
            "id": 202,
            "zona_id": 1,
            "nombre": "Tarantula",
            "tipo": "ARANA",
            "costo_hormigas": 3,
            "estado": "activa",
            "hora_deteccion": "2025-11-25T21:55:34.253818",
            "hora_resolucion": null
          }
        ]
        """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(json));

        List<Threat> active = externalService.getActiveThreats();

        assertNotNull(active);
        assertEquals(2, active.size());
        assertTrue(active.stream().anyMatch(t -> t.getId() == 200));
        assertTrue(active.stream().anyMatch(t -> t.getId() == 202));
        assertFalse(active.stream().anyMatch(t -> t.getId() == 201));
    }
}
