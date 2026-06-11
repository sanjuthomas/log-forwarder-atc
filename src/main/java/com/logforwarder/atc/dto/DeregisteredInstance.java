package com.logforwarder.atc.dto;

import java.util.UUID;

public record DeregisteredInstance(
        UUID id,
        String hostname,
        long processId
) {
}
