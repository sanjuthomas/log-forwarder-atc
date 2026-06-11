package com.logforwarder.atc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.logforwarder.atc.domain.FleetChangeType;

import java.util.UUID;

public record FleetChangeEvent(
        FleetChangeType type,
        @JsonProperty("instance_id") UUID instanceId,
        String hostname,
        @JsonProperty("process_id") long processId
) {
    public static FleetChangeEvent registered(RegistrationResponse response) {
        return new FleetChangeEvent(
                FleetChangeType.REGISTERED,
                response.id(),
                response.hostname(),
                response.processId()
        );
    }

    public static FleetChangeEvent deregistered(DeregisteredInstance instance) {
        return new FleetChangeEvent(
                FleetChangeType.DEREGISTERED,
                instance.id(),
                instance.hostname(),
                instance.processId()
        );
    }
}
