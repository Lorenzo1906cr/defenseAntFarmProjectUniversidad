package com.antfarmprojectcalidad.defense;

import com.antfarmprojectcalidad.defense.controller.HealthController;
import com.antfarmprojectcalidad.defense.model.Threat;
import com.antfarmprojectcalidad.defense.scheduler.ServiceMonitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthController.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void resetStatics() {
        ServiceMonitor.antFarmInDanger.set(false);
        ServiceMonitor.threats.clear();
        ServiceMonitor.threatsDefending.clear();
    }

    @Test
    void status_returnsExpectedJsonStructure() throws Exception {
        ServiceMonitor.antFarmInDanger.set(true);
        ServiceMonitor.threats.put(1, new Threat(1, "spider", "3"));
        ServiceMonitor.threatsDefending.put(1, Instant.now());

        mockMvc.perform(get("/health/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.farm_attacked").value(true))
                .andExpect(jsonPath("$.threats").isMap())
                .andExpect(jsonPath("$.threats_defending").isMap());
    }
}

