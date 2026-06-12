package com.logforwarder.atc.service;

import com.logforwarder.atc.client.LogForwarderAgentClient;
import com.logforwarder.atc.domain.Reachability;
import com.logforwarder.atc.dto.AgentMetricsResponse;
import com.logforwarder.atc.entity.InstanceMetricsSnapshot;
import com.logforwarder.atc.entity.LogForwarderInstance;
import com.logforwarder.atc.repository.InstanceMetricsSnapshotRepository;
import com.logforwarder.atc.repository.LogForwarderInstanceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstancePollingServiceTest {

    @Mock
    private LogForwarderInstanceRepository instanceRepository;

    @Mock
    private InstanceMetricsSnapshotRepository metricsRepository;

    @Mock
    private LogForwarderAgentClient agentClient;

    @InjectMocks
    private InstancePollingService pollingService;

    @Test
    void pollInstanceByIdThrowsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(instanceRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pollingService.pollInstance(id))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Instance not found");
    }

    @Test
    void pollAllInstancesDoesNothingWhenFleetIsEmpty() {
        when(instanceRepository.findAll()).thenReturn(List.of());

        pollingService.pollAllInstances();

        verifyNoInteractions(agentClient, metricsRepository);
    }

    @Test
    void pollAllInstancesMarksInstanceReachableAndPersistsSnapshot() {
        LogForwarderInstance instance = sampleInstance();
        when(instanceRepository.findAll()).thenReturn(List.of(instance));
        when(instanceRepository.save(instance)).thenReturn(instance);
        when(agentClient.poll(instance)).thenReturn(successfulPoll());

        pollingService.pollAllInstances();

        assertThat(instance.getReachability()).isEqualTo(Reachability.REACHABLE);
        assertThat(instance.getLastSeenAt()).isNotNull();

        ArgumentCaptor<InstanceMetricsSnapshot> snapshotCaptor = ArgumentCaptor.forClass(InstanceMetricsSnapshot.class);
        verify(metricsRepository).save(snapshotCaptor.capture());
        InstanceMetricsSnapshot snapshot = snapshotCaptor.getValue();
        assertThat(snapshot.getInstanceId()).isEqualTo(instance.getId());
        assertThat(snapshot.isHealthUp()).isTrue();
        assertThat(snapshot.isReadyUp()).isTrue();
        assertThat(snapshot.getFilesWatched()).isEqualTo(1L);
        assertThat(snapshot.getLinesPublished()).isEqualTo(2L);
        assertThat(snapshot.getLinesRead()).isEqualTo(3L);
        assertThat(snapshot.getLinesReplayed()).isEqualTo(4L);
    }

    @Test
    void pollInstanceByIdDelegatesToRegisteredInstance() {
        LogForwarderInstance instance = sampleInstance();
        UUID id = UUID.randomUUID();
        when(instanceRepository.findById(id)).thenReturn(Optional.of(instance));
        when(instanceRepository.save(instance)).thenReturn(instance);
        when(agentClient.poll(instance)).thenReturn(successfulPoll());

        pollingService.pollInstance(id);

        verify(agentClient).poll(instance);
        verify(metricsRepository).save(any(InstanceMetricsSnapshot.class));
    }

    @Test
    void pollMarksInstanceUnreachableWhenAllProbesFail() {
        LogForwarderInstance instance = sampleInstance();
        Instant lastSeen = Instant.parse("2026-06-11T12:00:00Z");
        instance.setLastSeenAt(lastSeen);
        when(instanceRepository.findAll()).thenReturn(List.of(instance));
        when(instanceRepository.save(instance)).thenReturn(instance);
        when(agentClient.poll(instance)).thenReturn(new LogForwarderAgentClient.AgentPollResult(
                false,
                false,
                null,
                "All agent probes failed"
        ));

        pollingService.pollAllInstances();

        assertThat(instance.getReachability()).isEqualTo(Reachability.UNREACHABLE);
        assertThat(instance.getLastSeenAt()).isEqualTo(lastSeen);

        ArgumentCaptor<InstanceMetricsSnapshot> snapshotCaptor = ArgumentCaptor.forClass(InstanceMetricsSnapshot.class);
        verify(metricsRepository).save(snapshotCaptor.capture());
        assertThat(snapshotCaptor.getValue().getPollError()).isEqualTo("All agent probes failed");
        assertThat(snapshotCaptor.getValue().getFilesWatched()).isNull();
    }

    @Test
    void pollMarksInstanceReachableWhenMetricsArePresent() {
        LogForwarderInstance instance = sampleInstance();
        when(instanceRepository.findAll()).thenReturn(List.of(instance));
        when(instanceRepository.save(instance)).thenReturn(instance);
        when(agentClient.poll(instance)).thenReturn(new LogForwarderAgentClient.AgentPollResult(
                false,
                false,
                new AgentMetricsResponse(5L, 6L, 7L, 8L, 0L, false, null, null),
                null
        ));

        pollingService.pollAllInstances();

        assertThat(instance.getReachability()).isEqualTo(Reachability.REACHABLE);
        verify(metricsRepository).save(any(InstanceMetricsSnapshot.class));
    }

    @Test
    void pollAllInstancesPollsEachRegisteredInstance() {
        LogForwarderInstance first = sampleInstance();
        LogForwarderInstance second = LogForwarderInstance.create(
                "app-server-02",
                54321,
                Instant.parse("2026-06-11T14:30:00Z"),
                8081,
                Instant.parse("2026-06-11T14:30:05Z")
        );
        when(instanceRepository.findAll()).thenReturn(List.of(first, second));
        when(instanceRepository.save(any(LogForwarderInstance.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(agentClient.poll(any(LogForwarderInstance.class))).thenReturn(successfulPoll());

        pollingService.pollAllInstances();

        verify(agentClient).poll(first);
        verify(agentClient).poll(second);
        verify(metricsRepository, org.mockito.Mockito.times(2)).save(any(InstanceMetricsSnapshot.class));
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

    private static LogForwarderAgentClient.AgentPollResult successfulPoll() {
        return new LogForwarderAgentClient.AgentPollResult(
                true,
                true,
                new AgentMetricsResponse(1L, 2L, 3L, 4L, 0L, false, 0.5, 1024L),
                null
        );
    }
}
