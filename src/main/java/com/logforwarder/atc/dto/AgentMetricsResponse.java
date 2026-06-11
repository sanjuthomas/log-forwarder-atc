package com.logforwarder.atc.dto;

public record AgentMetricsResponse(
        Long filesWatched,
        Long linesPublished,
        Long linesRead,
        Long pipelineBufferDepth,
        Boolean publishHibernating
) {
}
