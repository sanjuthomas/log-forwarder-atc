package com.logforwarder.atc.controller;

import com.logforwarder.atc.dto.InstanceSummaryResponse;
import com.logforwarder.atc.dto.MetricsSnapshotResponse;
import com.logforwarder.atc.service.FleetEventBroadcaster;
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

    public InstanceStatusController(
            InstanceStatusService statusService,
            FleetEventBroadcaster fleetEventBroadcaster
    ) {
        this.statusService = statusService;
        this.fleetEventBroadcaster = fleetEventBroadcaster;
    }

    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamFleetEvents() {
        return fleetEventBroadcaster.subscribe();
    }

    @GetMapping
    public List<InstanceSummaryResponse> listInstances() {
        return statusService.listInstances();
    }

    @GetMapping("/{id}")
    public InstanceSummaryResponse getInstance(@PathVariable UUID id) {
        return statusService.getInstance(id);
    }

    @GetMapping("/{id}/metrics")
    public List<MetricsSnapshotResponse> getMetrics(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "60") long lookbackMinutes
    ) {
        return statusService.getRecentMetrics(id, Duration.ofMinutes(lookbackMinutes));
    }
}
