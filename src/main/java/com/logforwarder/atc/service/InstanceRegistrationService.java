package com.logforwarder.atc.service;

import com.logforwarder.atc.domain.Reachability;
import com.logforwarder.atc.dto.DeregistrationRequest;
import com.logforwarder.atc.dto.DeregisteredInstance;
import com.logforwarder.atc.dto.RegistrationRequest;
import com.logforwarder.atc.dto.RegistrationResponse;
import com.logforwarder.atc.entity.LogForwarderInstance;
import com.logforwarder.atc.repository.LogForwarderInstanceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@Service
public class InstanceRegistrationService {

    private final LogForwarderInstanceRepository instanceRepository;
    private final FleetStatsService fleetStatsService;
    private final DeregisteredInstanceService deregisteredInstanceService;

    public InstanceRegistrationService(
            LogForwarderInstanceRepository instanceRepository,
            FleetStatsService fleetStatsService,
            DeregisteredInstanceService deregisteredInstanceService
    ) {
        this.instanceRepository = instanceRepository;
        this.fleetStatsService = fleetStatsService;
        this.deregisteredInstanceService = deregisteredInstanceService;
    }

    @Transactional
    public RegistrationResponse register(RegistrationRequest request) {
        Instant now = Instant.now();
        var existing = instanceRepository.findByHostnameAndPort(request.hostname(), request.port());

        if (existing.isPresent()) {
            LogForwarderInstance instance = existing.get();
            instance.setProcessId(request.processId());
            instance.setTimestamp(request.timestamp());
            instance.setRegisteredAt(now);
            instance.setReachability(Reachability.UNKNOWN);
            instanceRepository.save(instance);
            return toResponse(instance, false);
        }

        LogForwarderInstance instance = LogForwarderInstance.create(
                request.hostname(),
                request.processId(),
                request.timestamp(),
                request.port(),
                now
        );
        instanceRepository.save(instance);
        return toResponse(instance, true);
    }

    @Transactional(readOnly = true)
    public RegistrationResponse getRegistrationResponse(UUID id, boolean created) {
        LogForwarderInstance instance = instanceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instance not found"));
        return toResponse(instance, created);
    }

    @Transactional
    public DeregisteredInstance deregister(DeregistrationRequest request) {
        LogForwarderInstance instance = instanceRepository.findByHostnameAndPort(
                        request.hostname(),
                        request.port()
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No log-forwarder instance registered for hostname=%s port=%d"
                                .formatted(request.hostname(), request.port())
                ));
        DeregisteredInstance removed = new DeregisteredInstance(
                instance.getId(),
                instance.getHostname(),
                instance.getProcessId()
        );
        deregisteredInstanceService.recordDeregistration(instance);
        instanceRepository.delete(instance);
        fleetStatsService.recordDeregistration();
        return removed;
    }

    private RegistrationResponse toResponse(LogForwarderInstance instance, boolean created) {
        return new RegistrationResponse(
                instance.getId(),
                instance.getHostname(),
                instance.getProcessId(),
                instance.getTimestamp(),
                instance.getPort(),
                instance.getRegisteredAt(),
                instance.getReachability(),
                created
        );
    }
}
