package com.antfarmprojectcalidad.defense;

import com.antfarmprojectcalidad.defense.model.Threat;
import com.antfarmprojectcalidad.defense.service.ExternalService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ExternalServiceTest {
    @Test
    void shouldInitializeExternalService() {
        ExternalService service = new ExternalService("http://localhost:1234");

        assertNotNull(service);
        List<Threat> threats = service.getActiveThreats();

        assertNotNull(threats);
        assertTrue(threats.isEmpty(), "Initial implementation should return an empty list");
    }
}
