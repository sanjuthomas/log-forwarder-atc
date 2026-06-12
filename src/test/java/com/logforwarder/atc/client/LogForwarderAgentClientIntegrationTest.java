package com.logforwarder.atc.client;

import com.logforwarder.atc.config.AtcProperties;
import com.logforwarder.atc.entity.LogForwarderInstance;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogForwarderAgentClientIntegrationTest {

    private MockWebServer server;
    private LogForwarderAgentClient client;
    private LogForwarderInstance instance;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();

        String host = "127.0.0.1";
        WebClient webClient = WebClient.builder().build();
        AtcProperties properties = new AtcProperties(
                new AtcProperties.Polling(30_000, 3_000, 5_000),
                new AtcProperties.Agent("/health", "/ready", "/metrics")
        );
        client = new LogForwarderAgentClient(webClient, properties);
        instance = LogForwarderInstance.create(
                host,
                12345,
                Instant.parse("2026-06-11T14:30:00Z"),
                server.getPort(),
                Instant.parse("2026-06-11T14:30:05Z")
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void pollReturnsSuccessfulProbeAndMetrics() {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("{\"status\":\"UP\",\"process_id\":12345}"));
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("{\"status\":\"READY\",\"process_id\":12345}"));
        server.enqueue(new MockResponse().setBody("log_forwarder_files_watched 2\n"));

        LogForwarderAgentClient.AgentPollResult result = client.poll(instance);

        assertTrue(result.healthUp());
        assertTrue(result.readyUp());
        assertNotNull(result.metrics());
        assertEquals(2L, result.metrics().filesWatched());
        assertNull(result.error());
    }

    @Test
    void pollCapturesHttpErrorsAndMissingMetrics() {
        server.enqueue(new MockResponse().setResponseCode(503).setBody("unavailable"));
        server.enqueue(new MockResponse().setResponseCode(500).setBody("error"));
        server.enqueue(new MockResponse().setResponseCode(404).setBody("missing"));

        LogForwarderAgentClient.AgentPollResult result = client.poll(instance);

        assertFalse(result.healthUp());
        assertFalse(result.readyUp());
        assertNull(result.metrics());
        assertTrue(result.error().contains("Health probe returned HTTP 503"));
        assertTrue(result.error().contains("Ready probe returned HTTP 500"));
    }

    @Test
    void pollReportsProcessIdMismatchFromAgent() {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("{\"status\":\"UP\",\"process_id\":99999}"));
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("{\"status\":\"READY\",\"process_id\":12345}"));
        server.enqueue(new MockResponse().setBody("log_forwarder_files_watched 1\n"));

        LogForwarderAgentClient.AgentPollResult result = client.poll(instance);

        assertFalse(result.healthUp());
        assertTrue(result.readyUp());
        assertTrue(result.error().contains("Health probe process_id mismatch"));
    }
}
