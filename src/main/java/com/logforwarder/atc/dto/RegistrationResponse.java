package com.logforwarder.atc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.logforwarder.atc.domain.Reachability;

import java.time.Instant;
import java.util.UUID;

public record RegistrationResponse(
        UUID id,
        String hostname,
        @JsonProperty("process_id") long processId,
        @JsonProperty("timestamp") Instant timestamp,
        int port,
        @JsonProperty("registered_at") Instant registeredAt,
        Reachability reachability,
        boolean created
) {
}
