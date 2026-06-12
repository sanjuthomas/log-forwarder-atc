package com.logforwarder.atc.controller;

import com.logforwarder.atc.dto.DeregisteredInstanceSummaryResponse;
import com.logforwarder.atc.dto.FleetStatsResponse;
import com.logforwarder.atc.service.DeregisteredInstanceService;
import com.logforwarder.atc.service.FleetEventBroadcaster;
import com.logforwarder.atc.service.FleetStatsService;
import com.logforwarder.atc.service.InstanceStatusService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InstanceStatusController.class)
class InstanceStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InstanceStatusService statusService;

    @MockBean
    private FleetEventBroadcaster fleetEventBroadcaster;

    @MockBean
    private FleetStatsService fleetStatsService;

    @MockBean
    private DeregisteredInstanceService deregisteredInstanceService;

    @Test
    void fleetStatsReturnsDeregisteredTotal() throws Exception {
        when(fleetStatsService.getStats()).thenReturn(new FleetStatsResponse(7));

        mockMvc.perform(get("/api/instances/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deregistered_total").value(7));
    }

    @Test
    void listDeregisteredReturnsHistory() throws Exception {
        UUID instanceId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        UUID recordId = UUID.fromString("11111111-2222-3333-4444-555555555555");
        Instant registeredAt = Instant.parse("2026-06-11T14:00:00Z");
        Instant deregisteredAt = Instant.parse("2026-06-11T15:00:00Z");
        when(deregisteredInstanceService.listDeregistered()).thenReturn(List.of(
                new DeregisteredInstanceSummaryResponse(
                        recordId,
                        instanceId,
                        "app-server-01",
                        12345L,
                        8080,
                        registeredAt,
                        deregisteredAt
                )
        ));

        mockMvc.perform(get("/api/instances/deregistered"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hostname").value("app-server-01"))
                .andExpect(jsonPath("$[0].process_id").value(12345))
                .andExpect(jsonPath("$[0].port").value(8080))
                .andExpect(jsonPath("$[0].instance_id").value(instanceId.toString()))
                .andExpect(jsonPath("$[0].registered_at").value("2026-06-11T14:00:00Z"))
                .andExpect(jsonPath("$[0].deregistered_at").value("2026-06-11T15:00:00Z"));
    }

    @Test
    void listDeregisteredReturnsEmptyArrayWhenNoHistory() throws Exception {
        when(deregisteredInstanceService.listDeregistered()).thenReturn(List.of());

        mockMvc.perform(get("/api/instances/deregistered"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void deregisteredPathDoesNotMatchInstanceIdRoute() throws Exception {
        when(deregisteredInstanceService.listDeregistered()).thenReturn(List.of());

        mockMvc.perform(get("/api/instances/deregistered"))
                .andExpect(status().isOk());

        verify(deregisteredInstanceService).listDeregistered();
    }

    @Test
    void streamFleetEventsStartsAsyncSubscription() throws Exception {
        SseEmitter emitter = new SseEmitter(0L);
        when(fleetEventBroadcaster.subscribe()).thenReturn(emitter);

        mockMvc.perform(get("/api/instances/events"))
                .andExpect(request().asyncStarted());

        verify(fleetEventBroadcaster).subscribe();
        emitter.complete();
    }
}
