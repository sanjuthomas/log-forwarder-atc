package com.logforwarder.atc.client;

import com.logforwarder.atc.dto.AgentMetricsResponse;
import com.logforwarder.atc.dto.AgentProbeResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogForwarderAgentClientTest {

    @Test
    void validateProbeResponseAcceptsMatchingProcessId() {
        var outcome = LogForwarderAgentClient.validateProbeResponse(
                new AgentProbeResponse("READY", 12345),
                12345,
                "Ready"
        );

        assertTrue(outcome.success());
        assertNull(outcome.error());
    }

    @Test
    void validateProbeResponseRejectsMismatchedProcessId() {
        var outcome = LogForwarderAgentClient.validateProbeResponse(
                new AgentProbeResponse("READY", 82679),
                12345,
                "Ready"
        );

        assertFalse(outcome.success());
        assertEquals("Ready probe process_id mismatch: expected 12345, got 82679", outcome.error());
    }

    @Test
    void validateProbeResponseRejectsEmptyBody() {
        var outcome = LogForwarderAgentClient.validateProbeResponse(null, 12345, "Health");

        assertFalse(outcome.success());
        assertEquals("Health probe returned empty body", outcome.error());
    }

    @Test
    void buildPollErrorIncludesProcessIdMismatchMessages() {
        var health = LogForwarderAgentClient.ProbeOutcome.failure("Health probe process_id mismatch: expected 12345, got 999");
        var ready = LogForwarderAgentClient.ProbeOutcome.ok();

        String error = LogForwarderAgentClient.buildPollError(health, ready, new AgentMetricsResponse(1L, 2L, 3L, 0L, false));

        assertEquals("Health probe process_id mismatch: expected 12345, got 999", error);
    }

    @Test
    void buildPollErrorReturnsNullWhenProbesSucceed() {
        String error = LogForwarderAgentClient.buildPollError(
                LogForwarderAgentClient.ProbeOutcome.ok(),
                LogForwarderAgentClient.ProbeOutcome.ok(),
                new AgentMetricsResponse(1L, 2L, 3L, 0L, false)
        );

        assertNull(error);
    }
}
