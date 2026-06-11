package com.logforwarder.atc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AgentProbeResponse(
        String status,
        @JsonProperty("process_id") long processId
) {
}
