package com.logforwarder.atc.dto;

public record AgentMetricsResponse(
        Long filesMonitored,
        Long eventsProcessed,
        Long bytesRead
) {
}
