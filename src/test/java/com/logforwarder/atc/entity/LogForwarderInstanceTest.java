package com.logforwarder.atc.entity;

import com.logforwarder.atc.domain.Reachability;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class LogForwarderInstanceTest {

    @Test
    void createInitializesUnknownReachability() {
        Instant timestamp = Instant.parse("2026-06-11T14:30:00Z");
        Instant registeredAt = Instant.parse("2026-06-11T14:30:05Z");

        LogForwarderInstance instance = LogForwarderInstance.create(
                "app-server-01",
                12345,
                timestamp,
                8080,
                registeredAt
        );

        assertThat(instance.getHostname()).isEqualTo("app-server-01");
        assertThat(instance.getProcessId()).isEqualTo(12345);
        assertThat(instance.getTimestamp()).isEqualTo(timestamp);
        assertThat(instance.getPort()).isEqualTo(8080);
        assertThat(instance.getRegisteredAt()).isEqualTo(registeredAt);
        assertThat(instance.getReachability()).isEqualTo(Reachability.UNKNOWN);
        assertThat(instance.getLastSeenAt()).isNull();
    }

    @Test
    void settersUpdateMutableFields() {
        LogForwarderInstance instance = LogForwarderInstance.create(
                "app-server-01",
                12345,
                Instant.parse("2026-06-11T14:30:00Z"),
                8080,
                Instant.parse("2026-06-11T14:30:05Z")
        );
        Instant updatedTimestamp = Instant.parse("2026-06-11T15:00:00Z");
        Instant lastSeen = Instant.parse("2026-06-11T15:05:00Z");
        Instant registeredAt = Instant.parse("2026-06-11T14:00:00Z");

        instance.setHostname("worker-02");
        instance.setProcessId(67890);
        instance.setTimestamp(updatedTimestamp);
        instance.setPort(9090);
        instance.setRegisteredAt(registeredAt);
        instance.setLastSeenAt(lastSeen);
        instance.setReachability(Reachability.REACHABLE);

        assertThat(instance.getHostname()).isEqualTo("worker-02");
        assertThat(instance.getProcessId()).isEqualTo(67890);
        assertThat(instance.getTimestamp()).isEqualTo(updatedTimestamp);
        assertThat(instance.getPort()).isEqualTo(9090);
        assertThat(instance.getRegisteredAt()).isEqualTo(registeredAt);
        assertThat(instance.getLastSeenAt()).isEqualTo(lastSeen);
        assertThat(instance.getReachability()).isEqualTo(Reachability.REACHABLE);
    }
}
