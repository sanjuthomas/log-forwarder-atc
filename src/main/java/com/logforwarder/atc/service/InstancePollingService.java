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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

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
                metrics != null ? metrics.filesMonitored() : null,
                metrics != null ? metrics.eventsProcessed() : null,
                metrics != null ? metrics.bytesRead() : null,
                result.error()
        );
        metricsRepository.save(snapshot);

        log.info(
                "Polled {}:{} pid={} health={} ready={} reachability={}",
                instance.getHostname(),
                instance.getMetricsPort(),
                instance.getPid(),
                result.healthUp(),
                result.readyUp(),
                instance.getReachability()
        );
    }
}
