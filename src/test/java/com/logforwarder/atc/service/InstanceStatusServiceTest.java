package com.logforwarder.atc.service;

import com.logforwarder.atc.domain.Reachability;
import com.logforwarder.atc.dto.InstanceSummaryResponse;
import com.logforwarder.atc.dto.MetricsSnapshotResponse;
import com.logforwarder.atc.entity.InstanceMetricsSnapshot;
import com.logforwarder.atc.entity.LogForwarderInstance;
import com.logforwarder.atc.repository.InstanceMetricsSnapshotRepository;
import com.logforwarder.atc.repository.LogForwarderInstanceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstanceStatusServiceTest {

    @Mock
    private LogForwarderInstanceRepository instanceRepository;

    @Mock
    private InstanceMetricsSnapshotRepository metricsRepository;

    @InjectMocks
    private InstanceStatusService statusService;

    @Test
    void listInstancesReturnsSummariesWithoutMetricsWhenNoneExist() {
        LogForwarderInstance instance = sampleInstance();
        when(instanceRepository.findAll()).thenReturn(List.of(instance));
        when(metricsRepository.findTopByInstanceIdOrderByTimeDesc(instance.getId())).thenReturn(Optional.empty());

        List<InstanceSummaryResponse> summaries = statusService.listInstances();

        assertThat(summaries).hasSize(1);
        assertThat(summaries.getFirst().hostname()).isEqualTo("app-server-01");
        assertThat(summaries.getFirst().reachability()).isEqualTo(Reachability.UNKNOWN);
        assertThat(summaries.getFirst().latestMetrics()).isNull();
    }

    @Test
    void getInstanceReturnsSummaryWithLatestMetrics() {
        LogForwarderInstance instance = sampleInstance();
        Instant capturedAt = Instant.parse("2026-06-11T15:00:00Z");
        InstanceMetricsSnapshot snapshot = InstanceMetricsSnapshot.of(
                instance.getId(),
                capturedAt,
                true,
                true,
                1L,
                2L,
                3L,
                4L,
                0L,
                false,
                0.25,
                2048L,
                null
        );
        when(instanceRepository.findById(instance.getId())).thenReturn(Optional.of(instance));
        when(metricsRepository.findTopByInstanceIdOrderByTimeDesc(instance.getId())).thenReturn(Optional.of(snapshot));

        InstanceSummaryResponse summary = statusService.getInstance(instance.getId());

        assertThat(summary.id()).isEqualTo(instance.getId());
        assertThat(summary.latestMetrics()).isNotNull();
        assertThat(summary.latestMetrics().capturedAt()).isEqualTo(capturedAt);
        assertThat(summary.latestMetrics().filesWatched()).isEqualTo(1L);
        assertThat(summary.latestMetrics().linesReplayed()).isEqualTo(4L);
        assertThat(summary.latestMetrics().processMemoryUsage()).isEqualTo(2048L);
    }

    @Test
    void getInstanceThrowsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(instanceRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> statusService.getInstance(id))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Instance not found");
    }

    @Test
    void getRecentMetricsReturnsSnapshotsWithinLookback() {
        LogForwarderInstance instance = sampleInstance();
        Instant capturedAt = Instant.parse("2026-06-11T15:00:00Z");
        InstanceMetricsSnapshot snapshot = InstanceMetricsSnapshot.of(
                instance.getId(),
                capturedAt,
                false,
                true,
                9L,
                8L,
                7L,
                6L,
                1L,
                true,
                1.5,
                4096L,
                "ready probe failed"
        );
        when(instanceRepository.existsById(instance.getId())).thenReturn(true);
        when(metricsRepository.findByInstanceIdAndTimeAfterOrderByTimeAsc(
                org.mockito.ArgumentMatchers.eq(instance.getId()),
                org.mockito.ArgumentMatchers.any(Instant.class)
        )).thenReturn(List.of(snapshot));

        List<MetricsSnapshotResponse> metrics = statusService.getRecentMetrics(
                instance.getId(),
                Duration.ofMinutes(30)
        );

        assertThat(metrics).hasSize(1);
        assertThat(metrics.getFirst().instanceId()).isEqualTo(instance.getId());
        assertThat(metrics.getFirst().readyUp()).isTrue();
        assertThat(metrics.getFirst().publishHibernating()).isTrue();
        assertThat(metrics.getFirst().pollError()).isEqualTo("ready probe failed");
    }

    @Test
    void getRecentMetricsThrowsWhenInstanceMissing() {
        UUID id = UUID.randomUUID();
        when(instanceRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> statusService.getRecentMetrics(id, Duration.ofMinutes(5)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Instance not found");
    }

    private static LogForwarderInstance sampleInstance() {
        return LogForwarderInstance.create(
                "app-server-01",
                12345,
                Instant.parse("2026-06-11T14:30:00Z"),
                8080,
                Instant.parse("2026-06-11T14:30:05Z")
        );
    }
}
