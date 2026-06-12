package com.logforwarder.atc.service;

import com.logforwarder.atc.dto.DeregisteredInstanceSummaryResponse;
import com.logforwarder.atc.entity.DeregisteredInstanceRecord;
import com.logforwarder.atc.entity.LogForwarderInstance;
import com.logforwarder.atc.repository.DeregisteredInstanceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeregisteredInstanceServiceTest {

    @Mock
    private DeregisteredInstanceRepository repository;

    @InjectMocks
    private DeregisteredInstanceService deregisteredInstanceService;

    @Test
    void recordDeregistrationPersistsInstanceSnapshot() {
        Instant registeredAt = Instant.parse("2026-06-11T14:00:00Z");
        Instant timestamp = Instant.parse("2026-06-11T14:30:00Z");
        LogForwarderInstance instance = LogForwarderInstance.create(
                "app-server-01",
                12345,
                timestamp,
                8080,
                registeredAt
        );
        when(repository.save(any(DeregisteredInstanceRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Instant before = Instant.now();
        deregisteredInstanceService.recordDeregistration(instance);
        Instant after = Instant.now();

        ArgumentCaptor<DeregisteredInstanceRecord> captor = ArgumentCaptor.forClass(DeregisteredInstanceRecord.class);
        verify(repository).save(captor.capture());

        DeregisteredInstanceRecord saved = captor.getValue();
        assertThat(saved.getInstanceId()).isEqualTo(instance.getId());
        assertThat(saved.getHostname()).isEqualTo("app-server-01");
        assertThat(saved.getProcessId()).isEqualTo(12345);
        assertThat(saved.getPort()).isEqualTo(8080);
        assertThat(saved.getRegisteredAt()).isEqualTo(registeredAt);
        assertThat(saved.getDeregisteredAt()).isBetween(before, after);
    }

    @Test
    void listDeregisteredMapsRecordsToResponses() {
        Instant registeredAt = Instant.parse("2026-06-11T14:00:00Z");
        Instant deregisteredAt = Instant.parse("2026-06-11T15:00:00Z");
        LogForwarderInstance instance = LogForwarderInstance.create(
                "app-server-01",
                12345,
                Instant.parse("2026-06-11T14:30:00Z"),
                8080,
                registeredAt
        );
        DeregisteredInstanceRecord record = DeregisteredInstanceRecord.from(instance, deregisteredAt);
        when(repository.findAllByOrderByDeregisteredAtDesc()).thenReturn(List.of(record));

        List<DeregisteredInstanceSummaryResponse> responses = deregisteredInstanceService.listDeregistered();

        assertThat(responses).hasSize(1);
        DeregisteredInstanceSummaryResponse response = responses.getFirst();
        assertThat(response.hostname()).isEqualTo("app-server-01");
        assertThat(response.processId()).isEqualTo(12345);
        assertThat(response.port()).isEqualTo(8080);
        assertThat(response.instanceId()).isEqualTo(instance.getId());
        assertThat(response.registeredAt()).isEqualTo(registeredAt);
        assertThat(response.deregisteredAt()).isEqualTo(deregisteredAt);
    }

    @Test
    void listDeregisteredReturnsEmptyListWhenNoHistory() {
        when(repository.findAllByOrderByDeregisteredAtDesc()).thenReturn(List.of());

        assertThat(deregisteredInstanceService.listDeregistered()).isEmpty();
    }
}
