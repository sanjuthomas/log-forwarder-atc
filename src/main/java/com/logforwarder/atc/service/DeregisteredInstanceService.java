package com.logforwarder.atc.service;

import com.logforwarder.atc.dto.DeregisteredInstanceSummaryResponse;
import com.logforwarder.atc.entity.DeregisteredInstanceRecord;
import com.logforwarder.atc.entity.LogForwarderInstance;
import com.logforwarder.atc.repository.DeregisteredInstanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class DeregisteredInstanceService {

    private final DeregisteredInstanceRepository repository;

    public DeregisteredInstanceService(DeregisteredInstanceRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void recordDeregistration(LogForwarderInstance instance) {
        repository.save(DeregisteredInstanceRecord.from(instance, Instant.now()));
    }

    @Transactional(readOnly = true)
    public List<DeregisteredInstanceSummaryResponse> listDeregistered() {
        return repository.findAllByOrderByDeregisteredAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    private DeregisteredInstanceSummaryResponse toResponse(DeregisteredInstanceRecord record) {
        return new DeregisteredInstanceSummaryResponse(
                record.getId(),
                record.getInstanceId(),
                record.getHostname(),
                record.getProcessId(),
                record.getPort(),
                record.getRegisteredAt(),
                record.getDeregisteredAt()
        );
    }
}
