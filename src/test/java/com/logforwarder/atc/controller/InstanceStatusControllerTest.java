package com.logforwarder.atc.controller;

import com.logforwarder.atc.domain.Reachability;
import com.logforwarder.atc.dto.DeregisteredInstanceSummaryResponse;
import com.logforwarder.atc.dto.FleetStatsResponse;
import com.logforwarder.atc.dto.InstanceSummaryResponse;
import com.logforwarder.atc.dto.MetricsSnapshotResponse;
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

    @Test
    void listInstancesReturnsFleetSummaries() throws Exception {
        UUID instanceId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        when(statusService.listInstances()).thenReturn(List.of(
                new InstanceSummaryResponse(
                        instanceId,
                        "app-server-01",
                        12345,
                        Instant.parse("2026-06-11T14:30:00Z"),
                        8080,
                        Instant.parse("2026-06-11T14:30:05Z"),
                        Instant.parse("2026-06-11T15:00:00Z"),
                        Reachability.REACHABLE,
                        null
                )
        ));

        mockMvc.perform(get("/api/instances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hostname").value("app-server-01"))
                .andExpect(jsonPath("$[0].reachability").value("REACHABLE"));
    }

    @Test
    void getInstanceReturnsSummary() throws Exception {
        UUID instanceId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        when(statusService.getInstance(instanceId)).thenReturn(new InstanceSummaryResponse(
                instanceId,
                "app-server-01",
                12345,
                Instant.parse("2026-06-11T14:30:00Z"),
                8080,
                Instant.parse("2026-06-11T14:30:05Z"),
                null,
                Reachability.UNKNOWN,
                null
        ));

        mockMvc.perform(get("/api/instances/" + instanceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.process_id").value(12345))
                .andExpect(jsonPath("$.port").value(8080));
    }

    @Test
    void getMetricsUsesLookbackMinutes() throws Exception {
        UUID instanceId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        when(statusService.getRecentMetrics(instanceId, java.time.Duration.ofMinutes(15))).thenReturn(List.of(
                new MetricsSnapshotResponse(
                        instanceId,
                        Instant.parse("2026-06-11T15:00:00Z"),
                        true,
                        true,
                        1L,
                        2L,
                        3L,
                        4L,
                        0L,
                        false,
                        0.5,
                        1024L,
                        null
                )
        ));

        mockMvc.perform(get("/api/instances/" + instanceId + "/metrics").param("lookbackMinutes", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].lines_read").value(3))
                .andExpect(jsonPath("$[0].lines_replayed").value(4));
    }
}
