package com.logforwarder.atc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FleetStatsResponse(
        @JsonProperty("deregistered_total") long deregisteredTotal
) {
}
