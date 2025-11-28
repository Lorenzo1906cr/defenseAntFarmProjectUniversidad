package com.antfarmprojectcalidad.defense.service;

import com.antfarmprojectcalidad.defense.model.Threat;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(initializers = ExternalServiceTest.MockServerInitializer.class)
public class ExternalServiceTest {

    private static MockWebServer mockWebServer;

    @Autowired
    private ExternalService externalService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    static class MockServerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext ctx) {
            TestPropertyValues.of(
                    "url.entorno=http://localhost:" + mockWebServer.getPort()
            ).applyTo(ctx.getEnvironment());
        }
    }

    @Test
    void testGetActiveThreats() throws Exception {

        // Prepare a mock JSON response
        List<Threat> mockThreats = List.of(
                new Threat(1, "Amenaza 1", "activa"),
                new Threat(2, "Amenaza 2", "inactiva")
        );

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(mockThreats))
                .addHeader("Content-Type", "application/json"));

        List<Threat> result = externalService.getActiveThreats();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getNombre()).isEqualTo("Amenaza 1");
        assertThat(result.get(1).getNombre()).isEqualTo("Amenaza 2");
    }
}
