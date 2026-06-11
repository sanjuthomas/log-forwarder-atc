package com.logforwarder.atc.dto;

import java.time.Instant;
import java.util.UUID;

public record MetricsSnapshotResponse(
        UUID instanceId,
        Instant time,
        boolean healthUp,
        boolean readyUp,
        Long filesMonitored,
        Long eventsProcessed,
        Long bytesRead,
        String pollError
) {
}
