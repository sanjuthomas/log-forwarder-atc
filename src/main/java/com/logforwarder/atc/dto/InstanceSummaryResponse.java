package com.logforwarder.atc.dto;

import com.logforwarder.atc.domain.Reachability;

import java.time.Instant;
import java.util.UUID;

public record InstanceSummaryResponse(
        UUID id,
        String hostname,
        long pid,
        Instant startTime,
        int healthPort,
        int readyPort,
        int metricsPort,
        Instant registeredAt,
        Instant lastSeenAt,
        Reachability reachability,
        LatestMetrics latestMetrics
) {
    public record LatestMetrics(
            Instant capturedAt,
            boolean healthUp,
            boolean readyUp,
            Long filesMonitored,
            Long eventsProcessed,
            Long bytesRead,
            String pollError
    ) {
    }
}
