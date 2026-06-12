package com.logforwarder.atc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record DeregisteredInstanceSummaryResponse(
        UUID id,
        @JsonProperty("instance_id") UUID instanceId,
        String hostname,
        @JsonProperty("process_id") long processId,
        int port,
        @JsonProperty("registered_at") Instant registeredAt,
        @JsonProperty("deregistered_at") Instant deregisteredAt
) {
}
