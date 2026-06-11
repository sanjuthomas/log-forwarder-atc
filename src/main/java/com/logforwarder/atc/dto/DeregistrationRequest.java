package com.logforwarder.atc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record DeregistrationRequest(
        @NotBlank String hostname,
        @Positive long pid
) {
}
