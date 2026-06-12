package com.logforwarder.atc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeregistrationRequest(
        @NotBlank String hostname,
        @Min(1) @Max(65535) int port,
        @JsonProperty("process_id") @Positive Long processId,
        @JsonProperty("timestamp") Instant timestamp
) {
}
