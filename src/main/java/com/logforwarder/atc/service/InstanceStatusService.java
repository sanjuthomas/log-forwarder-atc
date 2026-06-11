package com.logforwarder.atc.service;

import com.logforwarder.atc.dto.InstanceSummaryResponse;
import com.logforwarder.atc.dto.MetricsSnapshotResponse;
import com.logforwarder.atc.entity.InstanceMetricsSnapshot;
import com.logforwarder.atc.entity.LogForwarderInstance;
import com.logforwarder.atc.repository.InstanceMetricsSnapshotRepository;
import com.logforwarder.atc.repository.LogForwarderInstanceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class InstanceStatusService {

    private final LogForwarderInstanceRepository instanceRepository;
    private final InstanceMetricsSnapshotRepository metricsRepository;

    public InstanceStatusService(
            LogForwarderInstanceRepository instanceRepository,
            InstanceMetricsSnapshotRepository metricsRepository
    ) {
        this.instanceRepository = instanceRepository;
        this.metricsRepository = metricsRepository;
    }

    public List<InstanceSummaryResponse> listInstances() {
        return instanceRepository.findAll().stream()
                .map(this::toSummary)
                .toList();
    }

    public InstanceSummaryResponse getInstance(UUID id) {
        LogForwarderInstance instance = instanceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instance not found"));
        return toSummary(instance);
    }

    public List<MetricsSnapshotResponse> getRecentMetrics(UUID id, Duration lookback) {
        if (!instanceRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Instance not found");
        }

        Instant since = Instant.now().minus(lookback);
        return metricsRepository.findByInstanceIdAndTimeAfterOrderByTimeAsc(id, since).stream()
                .map(this::toMetricsResponse)
                .toList();
    }

    private InstanceSummaryResponse toSummary(LogForwarderInstance instance) {
        InstanceSummaryResponse.LatestMetrics latestMetrics = metricsRepository
                .findTopByInstanceIdOrderByTimeDesc(instance.getId())
                .map(this::toLatestMetrics)
                .orElse(null);

        return new InstanceSummaryResponse(
                instance.getId(),
                instance.getHostname(),
                instance.getPid(),
                instance.getStartTime(),
                instance.getHealthPort(),
                instance.getReadyPort(),
                instance.getMetricsPort(),
                instance.getRegisteredAt(),
                instance.getLastSeenAt(),
                instance.getReachability(),
                latestMetrics
        );
    }

    private InstanceSummaryResponse.LatestMetrics toLatestMetrics(InstanceMetricsSnapshot snapshot) {
        return new InstanceSummaryResponse.LatestMetrics(
                snapshot.getTime(),
                snapshot.isHealthUp(),
                snapshot.isReadyUp(),
                snapshot.getFilesMonitored(),
                snapshot.getEventsProcessed(),
                snapshot.getBytesRead(),
                snapshot.getPollError()
        );
    }

    private MetricsSnapshotResponse toMetricsResponse(InstanceMetricsSnapshot snapshot) {
        return new MetricsSnapshotResponse(
                snapshot.getInstanceId(),
                snapshot.getTime(),
                snapshot.isHealthUp(),
                snapshot.isReadyUp(),
                snapshot.getFilesMonitored(),
                snapshot.getEventsProcessed(),
                snapshot.getBytesRead(),
                snapshot.getPollError()
        );
    }
}
