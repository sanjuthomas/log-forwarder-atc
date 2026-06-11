package com.logforwarder.atc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record MetricsSnapshotResponse(
        @JsonProperty("instance_id") UUID instanceId,
        Instant time,
        @JsonProperty("health_up") boolean healthUp,
        @JsonProperty("ready_up") boolean readyUp,
        @JsonProperty("files_watched") Long filesWatched,
        @JsonProperty("lines_published") Long linesPublished,
        @JsonProperty("lines_read") Long linesRead,
        @JsonProperty("pipeline_buffer_depth") Long pipelineBufferDepth,
        @JsonProperty("publish_hibernating") Boolean publishHibernating,
        @JsonProperty("poll_error") String pollError
) {
}
