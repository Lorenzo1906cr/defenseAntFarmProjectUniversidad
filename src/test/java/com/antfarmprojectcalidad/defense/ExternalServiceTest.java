package com.antfarmprojectcalidad.defense;

import com.antfarmprojectcalidad.defense.model.Threat;
import com.antfarmprojectcalidad.defense.service.ExternalService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ExternalServiceTest {
    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void shouldFetchThreatsFromApi() throws Exception {
        // Arrange: fake JSON response
        String json = """
                [
                  {
                    "id": 1,
                    "zona_id": 1,
                    "nombre": "Spider",
                    "tipo": "ARANA",
                    "costo_hormigas": 3,
                    "estado": "activa",
                    "hora_deteccion": "2025-11-25T21:55:34",
                    "hora_resolucion": null
                  }
                ]
                """;

        mockWebServer.enqueue(
                new MockResponse()
                        .setBody(json)
                        .addHeader("Content-Type", "application/json")
        );

        String baseUrl = mockWebServer.url("/").toString();
        ExternalService service = new ExternalService();

        // Act
        List<Threat> result = service.getActiveThreats(baseUrl); // still minimal implementation, so test fails

        // Assert — should return one threat from API
        assertEquals(1, result.size());
        assertEquals("Spider", result.get(0).getNombre());
    }
}
