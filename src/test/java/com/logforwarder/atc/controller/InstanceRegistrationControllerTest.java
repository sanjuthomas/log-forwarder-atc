package com.logforwarder.atc.controller;

import com.logforwarder.atc.dto.DeregistrationRequest;
import com.logforwarder.atc.dto.DeregisteredInstance;
import com.logforwarder.atc.dto.FleetChangeEvent;
import com.logforwarder.atc.dto.RegistrationRequest;
import com.logforwarder.atc.dto.RegistrationResponse;
import com.logforwarder.atc.domain.Reachability;
import com.logforwarder.atc.service.FleetEventBroadcaster;
import com.logforwarder.atc.service.InstancePollingService;
import com.logforwarder.atc.service.InstanceRegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@WebMvcTest(InstanceRegistrationController.class)
class InstanceRegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InstanceRegistrationService registrationService;

    @MockitoBean
    private InstancePollingService pollingService;

    @MockitoBean
    private FleetEventBroadcaster fleetEventBroadcaster;

    @Test
    void registerReturnsCreatedForNewInstance() throws Exception {
        UUID id = UUID.randomUUID();
        RegistrationResponse registered = new RegistrationResponse(
                id,
                "host-1",
                999,
                Instant.parse("2026-06-11T16:00:00Z"),
                8080,
                Instant.parse("2026-06-11T16:01:00Z"),
                Reachability.UNKNOWN,
                true
        );
        RegistrationResponse polled = new RegistrationResponse(
                id,
                "host-1",
                999,
                Instant.parse("2026-06-11T16:00:00Z"),
                8080,
                Instant.parse("2026-06-11T16:01:00Z"),
                Reachability.REACHABLE,
                true
        );
        when(registrationService.register(any(RegistrationRequest.class))).thenReturn(registered);
        when(registrationService.getRegistrationResponse(eq(id), eq(true))).thenReturn(polled);

        mockMvc.perform(put("/api/instances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "hostname": "host-1",
                                  "port": 8080,
                                  "process_id": 999,
                                  "timestamp": "2026-06-11T16:00:00Z"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.hostname").value("host-1"))
                .andExpect(jsonPath("$.process_id").value(999))
                .andExpect(jsonPath("$.port").value(8080))
                .andExpect(jsonPath("$.created").value(true))
                .andExpect(jsonPath("$.reachability").value("REACHABLE"));

        verify(pollingService).pollInstance(id);
        verify(fleetEventBroadcaster).broadcast(FleetChangeEvent.registered(polled));
    }

    @Test
    void deregisterReturnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        when(registrationService.deregister(any(DeregistrationRequest.class)))
                .thenReturn(new DeregisteredInstance(id, "host-1", 999));

        mockMvc.perform(delete("/api/instances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "hostname": "host-1",
                                  "port": 8080,
                                  "process_id": 999,
                                  "timestamp": "2026-06-11T16:00:00Z"
                                }
                                """))
                .andExpect(status().isNoContent());

        verify(fleetEventBroadcaster).broadcast(
                FleetChangeEvent.deregistered(new DeregisteredInstance(id, "host-1", 999))
        );
    }

    @Test
    void deregisterAcceptsMinimalHostnameAndPortPayload() throws Exception {
        UUID id = UUID.randomUUID();
        when(registrationService.deregister(any(DeregistrationRequest.class)))
                .thenReturn(new DeregisteredInstance(id, "host-1", 8080));

        mockMvc.perform(delete("/api/instances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "hostname": "host-1",
                                  "port": 8080
                                }
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void deregisterReturnsNotFoundWhenMissing() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "not found"))
                .when(registrationService)
                .deregister(any(DeregistrationRequest.class));

        mockMvc.perform(delete("/api/instances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "hostname": "missing-host",
                                  "port": 8080,
                                  "process_id": 1,
                                  "timestamp": "2026-06-11T16:00:00Z"
                                }
                                """))
                .andExpect(status().isNotFound());
    }
}
