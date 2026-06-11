package com.logforwarder.atc.service;

import com.logforwarder.atc.domain.Reachability;
import com.logforwarder.atc.dto.DeregistrationRequest;
import com.logforwarder.atc.dto.RegistrationRequest;
import com.logforwarder.atc.dto.RegistrationResponse;
import com.logforwarder.atc.entity.LogForwarderInstance;
import com.logforwarder.atc.repository.LogForwarderInstanceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
public class InstanceRegistrationService {

    private final LogForwarderInstanceRepository instanceRepository;

    public InstanceRegistrationService(LogForwarderInstanceRepository instanceRepository) {
        this.instanceRepository = instanceRepository;
    }

    @Transactional
    public RegistrationResponse register(RegistrationRequest request) {
        Instant now = Instant.now();
        var existing = instanceRepository.findByHostnameAndPid(request.hostname(), request.pid());

        if (existing.isPresent()) {
            LogForwarderInstance instance = existing.get();
            instance.setStartTime(request.startTime());
            instance.setHealthPort(request.healthPort());
            instance.setReadyPort(request.readyPort());
            instance.setMetricsPort(request.metricsPort());
            instance.setRegisteredAt(now);
            instance.setReachability(Reachability.UNKNOWN);
            instanceRepository.save(instance);
            return toResponse(instance, false);
        }

        LogForwarderInstance instance = LogForwarderInstance.create(
                request.hostname(),
                request.pid(),
                request.startTime(),
                request.healthPort(),
                request.readyPort(),
                request.metricsPort(),
                now
        );
        instanceRepository.save(instance);
        return toResponse(instance, true);
    }

    @Transactional
    public void deregister(DeregistrationRequest request) {
        LogForwarderInstance instance = instanceRepository.findByHostnameAndPid(request.hostname(), request.pid())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No log-forwarder instance registered for hostname=%s pid=%d"
                                .formatted(request.hostname(), request.pid())
                ));
        instanceRepository.delete(instance);
    }

    private RegistrationResponse toResponse(LogForwarderInstance instance, boolean created) {
        return new RegistrationResponse(
                instance.getId(),
                instance.getHostname(),
                instance.getPid(),
                instance.getStartTime(),
                instance.getHealthPort(),
                instance.getReadyPort(),
                instance.getMetricsPort(),
                instance.getRegisteredAt(),
                instance.getReachability(),
                created
        );
    }
}
