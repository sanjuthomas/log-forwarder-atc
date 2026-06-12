package com.logforwarder.atc.scheduler;

import com.logforwarder.atc.service.InstancePollingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InstancePollingSchedulerTest {

    @Mock
    private InstancePollingService pollingService;

    @InjectMocks
    private InstancePollingScheduler scheduler;

    @Test
    void pollRegisteredInstancesDelegatesToPollingService() {
        scheduler.pollRegisteredInstances();

        verify(pollingService).pollAllInstances();
    }
}
