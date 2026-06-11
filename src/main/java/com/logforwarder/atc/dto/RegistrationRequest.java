package com.logforwarder.atc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

public record RegistrationRequest(
        @NotBlank String hostname,
        @Min(1) @Max(65535) int port,
        @JsonProperty("process_id") @Positive long processId,
        @JsonProperty("timestamp") @NotNull Instant timestamp
) {
}
