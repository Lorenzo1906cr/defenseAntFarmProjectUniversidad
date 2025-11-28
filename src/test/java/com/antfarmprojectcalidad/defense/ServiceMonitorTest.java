package com.antfarmprojectcalidad.defense;

import com.antfarmprojectcalidad.defense.model.Threat;
import com.antfarmprojectcalidad.defense.scheduler.ServiceMonitor;
import com.antfarmprojectcalidad.defense.service.ExternalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

public class ServiceMonitorTest {
    private ExternalService externalService;
    private ServiceMonitor serviceMonitor;

    @BeforeEach
    void setUp() {
        externalService = mock(ExternalService.class);
        serviceMonitor = new ServiceMonitor(externalService);
    }

    @Test
    void checkForUpdates_callsExternalServiceAndIterates() {
        Threat t1 = new Threat(1, "Threat A", "activa");
        Threat t2 = new Threat(2, "Threat B", "inactiva");

        when(externalService.getActiveThreats())
                .thenReturn(List.of(t1, t2));

        serviceMonitor.checkForUpdates();

        verify(externalService, times(1)).getActiveThreats();
    }

    @Test
    void checkForUpdates_worksWhenListIsEmpty() {
        when(externalService.getActiveThreats()).thenReturn(List.of());

        serviceMonitor.checkForUpdates();

        verify(externalService, times(1)).getActiveThreats();
    }
}
