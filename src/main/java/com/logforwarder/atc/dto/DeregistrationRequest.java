package com.logforwarder.atc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record DeregistrationRequest(
        @NotBlank String hostname,
        @JsonProperty("process_id") @Positive long processId
) {
}
