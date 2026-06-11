package com.logforwarder.atc.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

public record RegistrationRequest(
        @NotBlank String hostname,
        @NotNull Instant startTime,
        @Min(1) @Max(65535) int healthPort,
        @Min(1) @Max(65535) int readyPort,
        @Min(1) @Max(65535) int metricsPort,
        @Positive long pid
) {
}
