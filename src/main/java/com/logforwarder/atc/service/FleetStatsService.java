package com.logforwarder.atc.service;

import com.logforwarder.atc.dto.FleetStatsResponse;
import com.logforwarder.atc.entity.FleetCounter;
import com.logforwarder.atc.repository.FleetCounterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FleetStatsService {

    static final String DEREGISTERED_TOTAL = "deregistered_total";

    private final FleetCounterRepository counterRepository;

    public FleetStatsService(FleetCounterRepository counterRepository) {
        this.counterRepository = counterRepository;
    }

    @Transactional(readOnly = true)
    public FleetStatsResponse getStats() {
        return new FleetStatsResponse(deregisteredTotal());
    }

    @Transactional
    public void recordDeregistration() {
        counterRepository.increment(DEREGISTERED_TOTAL);
    }

    private long deregisteredTotal() {
        return counterRepository.findById(DEREGISTERED_TOTAL)
                .map(FleetCounter::getValue)
                .orElse(0L);
    }
}
