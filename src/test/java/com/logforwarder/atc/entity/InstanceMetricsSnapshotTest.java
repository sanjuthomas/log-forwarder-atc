package com.logforwarder.atc.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InstanceMetricsSnapshotTest {

    @Test
    void ofPopulatesAllFields() {
        UUID instanceId = UUID.randomUUID();
        Instant time = Instant.parse("2026-06-11T15:00:00Z");

        InstanceMetricsSnapshot snapshot = InstanceMetricsSnapshot.of(
                instanceId,
                time,
                true,
                false,
                1L,
                2L,
                3L,
                4L,
                5L,
                true,
                0.75,
                8192L,
                "probe failed"
        );

        assertThat(snapshot.getInstanceId()).isEqualTo(instanceId);
        assertThat(snapshot.getTime()).isEqualTo(time);
        assertThat(snapshot.isHealthUp()).isTrue();
        assertThat(snapshot.isReadyUp()).isFalse();
        assertThat(snapshot.getFilesWatched()).isEqualTo(1L);
        assertThat(snapshot.getLinesPublished()).isEqualTo(2L);
        assertThat(snapshot.getLinesRead()).isEqualTo(3L);
        assertThat(snapshot.getLinesReplayed()).isEqualTo(4L);
        assertThat(snapshot.getPipelineBufferDepth()).isEqualTo(5L);
        assertThat(snapshot.getPublishHibernating()).isTrue();
        assertThat(snapshot.getProcessCpuUtilization()).isEqualTo(0.75);
        assertThat(snapshot.getProcessMemoryUsage()).isEqualTo(8192L);
        assertThat(snapshot.getPollError()).isEqualTo("probe failed");
    }

    @Test
    void metricsIdSupportsValueEquality() {
        Instant time = Instant.parse("2026-06-11T15:00:00Z");
        UUID instanceId = UUID.randomUUID();
        InstanceMetricsSnapshot.MetricsId first = new InstanceMetricsSnapshot.MetricsId(time, instanceId);
        InstanceMetricsSnapshot.MetricsId second = new InstanceMetricsSnapshot.MetricsId(time, instanceId);
        InstanceMetricsSnapshot.MetricsId different = new InstanceMetricsSnapshot.MetricsId(
                time.plusSeconds(1),
                instanceId
        );

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
        assertThat(first).isNotEqualTo(different);
        assertThat(first).isNotEqualTo("not-a-metrics-id");
    }
}
