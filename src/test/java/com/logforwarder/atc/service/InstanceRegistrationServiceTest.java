package com.logforwarder.atc.service;

import com.logforwarder.atc.domain.Reachability;
import com.logforwarder.atc.dto.DeregistrationRequest;
import com.logforwarder.atc.dto.DeregisteredInstance;
import com.logforwarder.atc.dto.RegistrationRequest;
import com.logforwarder.atc.entity.LogForwarderInstance;
import com.logforwarder.atc.repository.LogForwarderInstanceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstanceRegistrationServiceTest {

    @Mock
    private LogForwarderInstanceRepository instanceRepository;

    @Mock
    private FleetStatsService fleetStatsService;

    @Mock
    private DeregisteredInstanceService deregisteredInstanceService;

    @InjectMocks
    private InstanceRegistrationService registrationService;

    @Test
    void registerCreatesNewInstance() {
        RegistrationRequest request = new RegistrationRequest(
                "app-server-01",
                8080,
                12345,
                Instant.parse("2026-06-11T14:30:00Z")
        );
        when(instanceRepository.findByHostnameAndProcessId("app-server-01", 12345)).thenReturn(Optional.empty());
        when(instanceRepository.save(any(LogForwarderInstance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = registrationService.register(request);

        assertThat(response.created()).isTrue();
        assertThat(response.hostname()).isEqualTo("app-server-01");
        assertThat(response.processId()).isEqualTo(12345);
        assertThat(response.port()).isEqualTo(8080);
        assertThat(response.reachability()).isEqualTo(Reachability.UNKNOWN);
    }

    @Test
    void deregisterReturnsRemovedInstanceDetails() {
        UUID id = UUID.randomUUID();
        LogForwarderInstance instance = LogForwarderInstance.create(
                "app-server-01",
                12345,
                Instant.parse("2026-06-11T14:30:00Z"),
                8080,
                Instant.parse("2026-06-11T14:30:05Z")
        );
        when(instanceRepository.findByHostnameAndProcessId("app-server-01", 12345)).thenReturn(Optional.of(instance));

        DeregisteredInstance removed = registrationService.deregister(
                new DeregistrationRequest("app-server-01", 12345)
        );

        assertThat(removed.hostname()).isEqualTo("app-server-01");
        assertThat(removed.processId()).isEqualTo(12345);
        verify(deregisteredInstanceService).recordDeregistration(instance);
        verify(instanceRepository).delete(instance);
        verify(fleetStatsService).recordDeregistration();
    }

    @Test
    void deregisterThrowsWhenInstanceMissing() {
        when(instanceRepository.findByHostnameAndProcessId("missing", 1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationService.deregister(new DeregistrationRequest("missing", 1)))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void registerUpdatesExistingInstance() {
        LogForwarderInstance existing = LogForwarderInstance.create(
                "app-server-01",
                12345,
                Instant.parse("2026-06-11T14:00:00Z"),
                8081,
                Instant.parse("2026-06-11T14:00:05Z")
        );
        when(instanceRepository.findByHostnameAndProcessId("app-server-01", 12345)).thenReturn(Optional.of(existing));
        when(instanceRepository.save(existing)).thenReturn(existing);

        var response = registrationService.register(new RegistrationRequest(
                "app-server-01",
                8080,
                12345,
                Instant.parse("2026-06-11T14:30:00Z")
        ));

        assertThat(response.created()).isFalse();
        assertThat(response.port()).isEqualTo(8080);
        assertThat(response.timestamp()).isEqualTo(Instant.parse("2026-06-11T14:30:00Z"));
        assertThat(response.reachability()).isEqualTo(Reachability.UNKNOWN);

        ArgumentCaptor<LogForwarderInstance> captor = ArgumentCaptor.forClass(LogForwarderInstance.class);
        verify(instanceRepository).save(captor.capture());
        assertThat(captor.getValue().getPort()).isEqualTo(8080);
    }
}
