package com.antfarmprojectcalidad.defense;

import com.antfarmprojectcalidad.defense.model.MensajeRequest;
import com.antfarmprojectcalidad.defense.model.MensajeResponse;
import com.antfarmprojectcalidad.defense.model.Threat;
import com.antfarmprojectcalidad.defense.scheduler.ServiceMonitor;
import com.antfarmprojectcalidad.defense.service.CommunicationService;
import com.antfarmprojectcalidad.defense.service.ExternalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class ServiceMonitorTest {
    private ExternalService externalService;
    private ServiceMonitor serviceMonitor;

    @BeforeEach
    void setUp() {
        externalService = mock(ExternalService.class);
        CommunicationService communicationService = mock(CommunicationService.class);
        serviceMonitor = new ServiceMonitor(externalService, communicationService);
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

    @Test
    void testRequestSupport() {
        ExternalService externalService = mock(ExternalService.class);
        CommunicationService communicationService = mock(CommunicationService.class);

        ServiceMonitor monitor = new ServiceMonitor(externalService, communicationService);

        Threat threat = mock(Threat.class);
        when(threat.getId()).thenReturn(123);
        when(threat.getCosto_hormigas()).thenReturn("5");

        MensajeResponse fakeResponse = new MensajeResponse();
        fakeResponse.setId("RESP-555");
        when(communicationService.enviarMensaje(any(MensajeRequest.class)))
                .thenReturn(fakeResponse);

        MensajeResponse result = monitor.requestSupport(threat);

        assertNotNull(result);
        assertEquals("RESP-555", result.getId());

        ArgumentCaptor<MensajeRequest> captor = ArgumentCaptor.forClass(MensajeRequest.class);
        verify(communicationService, times(1)).enviarMensaje(captor.capture());

        MensajeRequest sent = captor.getValue();
        assertEquals("S05_DEF", sent.getEmisor());
        assertEquals("S03_REI", sent.getReceptor());

        assertNotNull(sent.getContenido());
        Map<String, Object> contenido = sent.getContenido();
        assertEquals("solicitud_hormigas", contenido.get("tipo"));

        Map<String, Object> inner = (Map<String, Object>) contenido.get("contenido");
        assertNotNull(inner);

        assertEquals(1, inner.get("request_ref"));
        assertEquals(123, inner.get("threat_id"));
        assertEquals("5", inner.get("ants_needed"));
    }
}
