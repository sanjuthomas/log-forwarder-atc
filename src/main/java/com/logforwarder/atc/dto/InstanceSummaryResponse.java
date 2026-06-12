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
            @JsonProperty("files_watched") Long filesWatched,
            @JsonProperty("lines_published") Long linesPublished,
            @JsonProperty("lines_read") Long linesRead,
            @JsonProperty("lines_replayed") Long linesReplayed,
            @JsonProperty("pipeline_buffer_depth") Long pipelineBufferDepth,
            @JsonProperty("publish_hibernating") Boolean publishHibernating,
            @JsonProperty("process_cpu_utilization") Double processCpuUtilization,
            @JsonProperty("process_memory_usage") Long processMemoryUsage,
            @JsonProperty("poll_error") String pollError
    ) {
    }
}
