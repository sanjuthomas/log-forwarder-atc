package com.logforwarder.atc.dto;

public record AgentMetricsResponse(
        Long filesWatched,
        Long linesPublished,
        Long linesRead,
        Long linesReplayed,
        Long pipelineBufferDepth,
        Boolean publishHibernating,
        Double processCpuUtilization,
        Long processMemoryUsage
) {
}
