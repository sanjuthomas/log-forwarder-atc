package com.logforwarder.atc.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record DeregistrationRequest(
        @NotBlank String hostname,
        @Min(1) @Max(65535) int port
) {
}
