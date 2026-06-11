package com.logforwarder.atc.controller;

import com.logforwarder.atc.dto.InstanceSummaryResponse;
import com.logforwarder.atc.dto.MetricsSnapshotResponse;
import com.logforwarder.atc.service.InstanceStatusService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/instances")
public class InstanceStatusController {

    private final InstanceStatusService statusService;

    public InstanceStatusController(InstanceStatusService statusService) {
        this.statusService = statusService;
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
