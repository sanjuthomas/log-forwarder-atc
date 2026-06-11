package com.logforwarder.atc.dto;

import com.logforwarder.atc.domain.Reachability;

import java.time.Instant;
import java.util.UUID;

public record RegistrationResponse(
        UUID id,
        String hostname,
        long pid,
        Instant startTime,
        int healthPort,
        int readyPort,
        int metricsPort,
        Instant registeredAt,
        Reachability reachability,
        boolean created
) {
}
