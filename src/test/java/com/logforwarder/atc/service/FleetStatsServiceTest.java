package com.logforwarder.atc.service;

import com.logforwarder.atc.entity.FleetCounter;
import com.logforwarder.atc.repository.FleetCounterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FleetStatsServiceTest {

    @Mock
    private FleetCounterRepository counterRepository;

    @InjectMocks
    private FleetStatsService fleetStatsService;

    @Test
    void getStatsReturnsDeregisteredTotal() {
        when(counterRepository.findById(FleetStatsService.DEREGISTERED_TOTAL))
                .thenReturn(Optional.of(new FleetCounter(FleetStatsService.DEREGISTERED_TOTAL, 12)));

        assertThat(fleetStatsService.getStats().deregisteredTotal()).isEqualTo(12);
    }

    @Test
    void recordDeregistrationIncrementsCounter() {
        fleetStatsService.recordDeregistration();

        verify(counterRepository).increment(FleetStatsService.DEREGISTERED_TOTAL);
    }
}
