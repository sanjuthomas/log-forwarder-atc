package com.logforwarder.atc.service;

import com.logforwarder.atc.client.LogForwarderAgentClient;
import com.logforwarder.atc.domain.Reachability;
import com.logforwarder.atc.dto.AgentMetricsResponse;
import com.logforwarder.atc.entity.InstanceMetricsSnapshot;
import com.logforwarder.atc.entity.LogForwarderInstance;
import com.logforwarder.atc.repository.InstanceMetricsSnapshotRepository;
import com.logforwarder.atc.repository.LogForwarderInstanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class InstancePollingService {

    private static final Logger log = LoggerFactory.getLogger(InstancePollingService.class);

    private final LogForwarderInstanceRepository instanceRepository;
    private final InstanceMetricsSnapshotRepository metricsRepository;
    private final LogForwarderAgentClient agentClient;

    public InstancePollingService(
            LogForwarderInstanceRepository instanceRepository,
            InstanceMetricsSnapshotRepository metricsRepository,
            LogForwarderAgentClient agentClient
    ) {
        this.instanceRepository = instanceRepository;
        this.metricsRepository = metricsRepository;
        this.agentClient = agentClient;
    }

    @Transactional
    public void pollInstance(UUID id) {
        LogForwarderInstance instance = instanceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instance not found"));
        pollInstance(instance);
    }

    @Transactional
    public void pollAllInstances() {
        List<LogForwarderInstance> instances = instanceRepository.findAll();
        if (instances.isEmpty()) {
            return;
        }

        log.debug("Polling {} registered log-forwarder instance(s)", instances.size());
        for (LogForwarderInstance instance : instances) {
            pollInstance(instance);
        }
    }

    private void pollInstance(LogForwarderInstance instance) {
        Instant now = Instant.now();
        LogForwarderAgentClient.AgentPollResult result = agentClient.poll(instance);

        boolean reachable = result.healthUp() || result.readyUp() || result.metrics() != null;
        instance.setReachability(reachable ? Reachability.REACHABLE : Reachability.UNREACHABLE);
        instance.setLastSeenAt(reachable ? now : instance.getLastSeenAt());
        instanceRepository.save(instance);

        AgentMetricsResponse metrics = result.metrics();
        InstanceMetricsSnapshot snapshot = InstanceMetricsSnapshot.of(
                instance.getId(),
                now,
                result.healthUp(),
                result.readyUp(),
                metrics != null ? metrics.filesWatched() : null,
                metrics != null ? metrics.linesPublished() : null,
                metrics != null ? metrics.linesRead() : null,
                metrics != null ? metrics.pipelineBufferDepth() : null,
                metrics != null ? metrics.publishHibernating() : null,
                result.error()
        );
        metricsRepository.save(snapshot);

        log.info(
                "Polled {}:{} process_id={} health={} ready={} reachability={}",
                instance.getHostname(),
                instance.getPort(),
                instance.getProcessId(),
                result.healthUp(),
                result.readyUp(),
                instance.getReachability()
        );
    }
}
