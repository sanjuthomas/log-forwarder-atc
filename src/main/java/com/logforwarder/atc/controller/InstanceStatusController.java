package com.logforwarder.atc.controller;

import com.logforwarder.atc.dto.DeregisteredInstanceSummaryResponse;
import com.logforwarder.atc.dto.FleetStatsResponse;
import com.logforwarder.atc.dto.InstanceSummaryResponse;
import com.logforwarder.atc.dto.MetricsSnapshotResponse;
import com.logforwarder.atc.service.DeregisteredInstanceService;
import com.logforwarder.atc.service.FleetEventBroadcaster;
import com.logforwarder.atc.service.FleetStatsService;
import com.logforwarder.atc.service.InstanceStatusService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/instances")
public class InstanceStatusController {

    private final InstanceStatusService statusService;
    private final FleetEventBroadcaster fleetEventBroadcaster;
    private final FleetStatsService fleetStatsService;
    private final DeregisteredInstanceService deregisteredInstanceService;

    public InstanceStatusController(
            InstanceStatusService statusService,
            FleetEventBroadcaster fleetEventBroadcaster,
            FleetStatsService fleetStatsService,
            DeregisteredInstanceService deregisteredInstanceService
    ) {
        this.statusService = statusService;
        this.fleetEventBroadcaster = fleetEventBroadcaster;
        this.fleetStatsService = fleetStatsService;
        this.deregisteredInstanceService = deregisteredInstanceService;
    }

    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamFleetEvents() {
        return fleetEventBroadcaster.subscribe();
    }

    @GetMapping
    public List<InstanceSummaryResponse> listInstances() {
        return statusService.listInstances();
    }

    @GetMapping("/stats")
    public FleetStatsResponse fleetStats() {
        return fleetStatsService.getStats();
    }

    @GetMapping("/deregistered")
    public List<DeregisteredInstanceSummaryResponse> listDeregistered() {
        return deregisteredInstanceService.listDeregistered();
    }

    @GetMapping("/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}")
    public InstanceSummaryResponse getInstance(@PathVariable UUID id) {
        return statusService.getInstance(id);
    }

    @GetMapping("/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}/metrics")
    public List<MetricsSnapshotResponse> getMetrics(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "60") long lookbackMinutes
    ) {
        return statusService.getRecentMetrics(id, Duration.ofMinutes(lookbackMinutes));
    }
}
