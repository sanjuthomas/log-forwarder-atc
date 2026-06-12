package com.logforwarder.atc.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class DeregisteredInstanceRecordTest {

    @Test
    void fromCopiesInstanceFields() {
        Instant registeredAt = Instant.parse("2026-06-11T14:00:00Z");
        Instant timestamp = Instant.parse("2026-06-11T14:30:00Z");
        Instant deregisteredAt = Instant.parse("2026-06-11T15:00:00Z");
        LogForwarderInstance instance = LogForwarderInstance.create(
                "worker-02",
                98765,
                timestamp,
                9090,
                registeredAt
        );

        DeregisteredInstanceRecord record = DeregisteredInstanceRecord.from(instance, deregisteredAt);

        assertThat(record.getInstanceId()).isEqualTo(instance.getId());
        assertThat(record.getHostname()).isEqualTo("worker-02");
        assertThat(record.getProcessId()).isEqualTo(98765);
        assertThat(record.getPort()).isEqualTo(9090);
        assertThat(record.getRegisteredAt()).isEqualTo(registeredAt);
        assertThat(record.getDeregisteredAt()).isEqualTo(deregisteredAt);
    }
}
