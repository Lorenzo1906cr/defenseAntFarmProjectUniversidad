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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ServiceMonitorTest {
    private ExternalService externalService;
    private CommunicationService communicationService;
    private ServiceMonitor serviceMonitor;

    @BeforeEach
    void setUp() {
        externalService = mock(ExternalService.class);
        communicationService = mock(CommunicationService.class);

        ServiceMonitor.antFarmInDanger.set(false);
        ServiceMonitor.threats.clear();
        ServiceMonitor.threatsWaitingForAnts.clear();

        serviceMonitor = new ServiceMonitor(externalService, communicationService);
    }

    @Test
    void checkForUpdates_callsExternalServiceAndIterates() {
        Threat t1 = new Threat(1, "Threat A", "activa");
        Threat t2 = new Threat(2, "Threat B", "inactiva");

        MensajeResponse fakeResponse = new MensajeResponse();
        fakeResponse.setId("RESP-555");
        when(communicationService.enviarMensaje(any(MensajeRequest.class)))
                .thenReturn(fakeResponse);

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

    @Test
    void testCheckForUpdatesPopulatesMapsAndDangerFlag() {
        Threat threat = new Threat();
        threat.setId(101);
        threat.setCosto_hormigas("5");

        when(externalService.getActiveThreats()).thenReturn(Collections.singletonList(threat));

        MensajeResponse response = new MensajeResponse();
        response.setMensaje("Mensaje creado con éxito");
        response.setId("msg-123");

        when(communicationService.enviarMensaje(any())).thenReturn(response);

        serviceMonitor.checkForUpdates();

        assertTrue(ServiceMonitor.antFarmInDanger.get(), "antFarmInDanger should be TRUE since 1 threat exists");
        assertEquals(1, ServiceMonitor.threats.size(), "threats map should contain the threat");
        assertTrue(ServiceMonitor.threats.containsKey(101), "Threat ID 101 must be stored");
        assertEquals(1, ServiceMonitor.threatsWaitingForAnts.size(), "threatsWaitingForAnts must contain the threat");

        verify(communicationService, times(1)).enviarMensaje(any());
    }

    @Test
    void checkIncomingMessages_callsCommunicationService() {
        ExternalService external = mock(ExternalService.class);
        CommunicationService comm = mock(CommunicationService.class);

        ServiceMonitor monitor = new ServiceMonitor(external, comm);

        when(comm.obtenerMensaje("S05_DEF"))
                .thenReturn(Collections.emptyList()); // avoid null

        monitor.checkIncomingMessages();

        verify(comm, times(1)).obtenerMensaje("S05_DEF"); // Fails at first
    }

    @Test
    void checkIncomingMessages_processesAsignacionMessage() throws Exception {
        ExternalService external = mock(ExternalService.class);
        CommunicationService comm = mock(CommunicationService.class);

        ServiceMonitor monitor = spy(new ServiceMonitor(external, comm));

        // fake JSON message
        MensajeResponse msg = new MensajeResponse();
        msg.setMensaje("""
            { "tipo": "asignacion_hormigas",
              "contenido": {
                   "request_ref": "req-1",
                   "ants": [ {"id":"A-1"} ]
              }}
            """);

        when(comm.obtenerMensaje("S05_DEF")).thenReturn(List.of(msg));

        monitor.checkIncomingMessages();

        assertEquals(
                List.of("asignacion_hormigas"),
                monitor.getProcessedTypesForTest()
        );
    }

    @Test
    void checkIncomingMessages_processesRechazoMessage() throws Exception {
        ExternalService external = mock(ExternalService.class);
        CommunicationService comm = mock(CommunicationService.class);

        ServiceMonitor monitor = spy(new ServiceMonitor(external, comm));

        MensajeResponse msg = new MensajeResponse();
        msg.setMensaje("""
            { "tipo": "rechazo_hormigas",
              "contenido": { "request_ref": "req-1", "motivo": "insuficientes" }}
            """);

        when(comm.obtenerMensaje("S05_DEF")).thenReturn(List.of(msg));

        monitor.checkIncomingMessages();

        assertEquals(
                List.of("rechazo_hormigas"),
                monitor.getProcessedTypesForTest()
        );
    }
}

