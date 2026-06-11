package com.logforwarder.atc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.logforwarder.atc.domain.Reachability;

import java.time.Instant;
import java.util.UUID;

public record InstanceSummaryResponse(
        UUID id,
        String hostname,
        @JsonProperty("process_id") long processId,
        @JsonProperty("timestamp") Instant timestamp,
        int port,
        @JsonProperty("registered_at") Instant registeredAt,
        @JsonProperty("last_seen_at") Instant lastSeenAt,
        Reachability reachability,
        @JsonProperty("latest_metrics") LatestMetrics latestMetrics
) {
    public record LatestMetrics(
            @JsonProperty("captured_at") Instant capturedAt,
            @JsonProperty("health_up") boolean healthUp,
            @JsonProperty("ready_up") boolean readyUp,
            @JsonProperty("files_monitored") Long filesMonitored,
            @JsonProperty("events_processed") Long eventsProcessed,
            @JsonProperty("bytes_read") Long bytesRead,
            @JsonProperty("poll_error") String pollError
    ) {
    }
}
