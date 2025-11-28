package com.antfarmprojectcalidad.defense;

import com.antfarmprojectcalidad.defense.model.Threat;
import com.antfarmprojectcalidad.defense.scheduler.ServiceMonitor;
import com.antfarmprojectcalidad.defense.service.ExternalService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;

import static org.mockito.Mockito.*;


class ThreatCheckerTest {

    @Test
    void testCheckForUpdatesCreatesAThreadPerThreat() {
        ExternalService externalService = mock(ExternalService.class);

        Threat threat1 = mock(Threat.class);
        Threat threat2 = mock(Threat.class);

        when(externalService.getActiveThreats()).thenReturn(Arrays.asList(threat1, threat2));

        ServiceMonitor checker = Mockito.spy(new ServiceMonitor(externalService));
        Thread mockThread = mock(Thread.class);

        doReturn(mockThread).when(checker).createThread(any());
        checker.checkForUpdates();

        verify(checker, times(2)).createThread(any());
        verify(mockThread, times(2)).start();
    }
}
