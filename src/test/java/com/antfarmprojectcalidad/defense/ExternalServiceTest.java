package com.antfarmprojectcalidad.defense;

import com.antfarmprojectcalidad.defense.service.ExternalService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExternalServiceTest {
    @Test
    void shouldReturnOnlyActiveThreats() throws IOException {
        MockWebServer server = new MockWebServer();
        server.start();

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("""
                            [
                              { "id": 1, "estado": "activa" },
                              { "id": 2, "estado": "resuelta" },
                              { "id": 3, "estado": "activa" }
                            ]
                        """));

        String baseUrl = server.url("/").toString();

        ExternalService externalService = new ExternalService(baseUrl);

        var result = externalService.getActiveThreats();

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(t -> t.getId() == 1));
        assertTrue(result.stream().anyMatch(t -> t.getId() == 3));

        server.shutdown();
    }
}
