package com.logforwarder.atc.scheduler;

import com.logforwarder.atc.service.InstancePollingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InstancePollingScheduler {

    private final InstancePollingService pollingService;

    public InstancePollingScheduler(InstancePollingService pollingService) {
        this.pollingService = pollingService;
    }

    @Scheduled(fixedDelayString = "${atc.polling.interval-ms:60000}")
    public void pollRegisteredInstances() {
        pollingService.pollAllInstances();
    }
}
