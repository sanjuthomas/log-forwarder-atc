package com.logforwarder.atc.dto;

import com.logforwarder.atc.domain.FleetChangeType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FleetChangeEventTest {

    @Test
    void registeredFactoryBuildsEventFromRegistrationResponse() {
        UUID id = UUID.randomUUID();
        RegistrationResponse response = new RegistrationResponse(
                id,
                "app-server-01",
                12345,
                Instant.parse("2026-06-11T14:30:00Z"),
                8080,
                Instant.parse("2026-06-11T14:30:05Z"),
                com.logforwarder.atc.domain.Reachability.REACHABLE,
                true
        );

        FleetChangeEvent event = FleetChangeEvent.registered(response);

        assertThat(event.type()).isEqualTo(FleetChangeType.REGISTERED);
        assertThat(event.instanceId()).isEqualTo(id);
        assertThat(event.hostname()).isEqualTo("app-server-01");
        assertThat(event.processId()).isEqualTo(12345);
    }

    @Test
    void deregisteredFactoryBuildsEventFromRemovedInstance() {
        UUID id = UUID.randomUUID();
        DeregisteredInstance removed = new DeregisteredInstance(id, "app-server-01", 12345);

        FleetChangeEvent event = FleetChangeEvent.deregistered(removed);

        assertThat(event.type()).isEqualTo(FleetChangeType.DEREGISTERED);
        assertThat(event.instanceId()).isEqualTo(id);
        assertThat(event.processId()).isEqualTo(12345);
    }
}
