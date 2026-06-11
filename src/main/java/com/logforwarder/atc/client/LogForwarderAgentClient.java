package com.logforwarder.atc.client;

import com.logforwarder.atc.config.AtcProperties;
import com.logforwarder.atc.dto.AgentMetricsResponse;
import com.logforwarder.atc.dto.AgentProbeResponse;
import com.logforwarder.atc.entity.LogForwarderInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class LogForwarderAgentClient {

    private static final Logger log = LoggerFactory.getLogger(LogForwarderAgentClient.class);

    private final WebClient webClient;
    private final AtcProperties properties;

    public LogForwarderAgentClient(WebClient webClient, AtcProperties properties) {
        this.webClient = webClient;
        this.properties = properties;
    }

    public AgentPollResult poll(LogForwarderInstance instance) {
        String baseHost = instance.getHostname();
        int port = instance.getPort();
        long expectedProcessId = instance.getProcessId();

        ProbeOutcome health = probeEndpoint(baseHost, port, properties.agent().healthPath(), expectedProcessId, "Health");
        ProbeOutcome ready = probeEndpoint(baseHost, port, properties.agent().readyPath(), expectedProcessId, "Ready");
        AgentMetricsResponse metrics = fetchMetrics(baseHost, port);

        String error = buildPollError(health, ready, metrics);

        return new AgentPollResult(health.success(), ready.success(), metrics, error);
    }

    static String buildPollError(ProbeOutcome health, ProbeOutcome ready, AgentMetricsResponse metrics) {
        List<String> errors = new ArrayList<>();
        if (health.error() != null) {
            errors.add(health.error());
        }
        if (ready.error() != null) {
            errors.add(ready.error());
        }
        if (!health.success() && !ready.success() && metrics == null && errors.isEmpty()) {
            errors.add("All agent probes failed");
        }
        if (errors.isEmpty()) {
            return null;
        }
        return String.join("; ", errors);
    }

    static ProbeOutcome validateProbeResponse(AgentProbeResponse response, long expectedProcessId, String probeName) {
        if (response == null) {
            return ProbeOutcome.failure(probeName + " probe returned empty body");
        }
        if (response.processId() != expectedProcessId) {
            return ProbeOutcome.failure(
                    "%s probe process_id mismatch: expected %d, got %d"
                            .formatted(probeName, expectedProcessId, response.processId())
            );
        }
        return ProbeOutcome.ok();
    }

    private ProbeOutcome probeEndpoint(
            String hostname,
            int port,
            String path,
            long expectedProcessId,
            String probeName
    ) {
        String url = agentUrl(hostname, port, path);
        try {
            AgentProbeResponse body = webClient.get()
                    .uri(url)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse -> clientResponse.createException())
                    .bodyToMono(AgentProbeResponse.class)
                    .timeout(readTimeout())
                    .block();
            ProbeOutcome outcome = validateProbeResponse(body, expectedProcessId, probeName);
            if (!outcome.success()) {
                log.debug("Agent probe {} failed validation: {}", url, outcome.error());
            }
            return outcome;
        } catch (WebClientResponseException ex) {
            log.debug("Agent probe {} returned HTTP {}: {}", url, ex.getStatusCode().value(), ex.getMessage());
            return ProbeOutcome.failure(probeName + " probe returned HTTP " + ex.getStatusCode().value());
        } catch (RuntimeException ex) {
            log.debug("Agent probe {} failed: {}", url, rootMessage(ex));
            return ProbeOutcome.failure(probeName + " probe failed: " + rootMessage(ex));
        }
    }

    private AgentMetricsResponse fetchMetrics(String hostname, int port) {
        String url = agentUrl(hostname, port, properties.agent().metricsPath());
        try {
            String body = webClient.get()
                    .uri(url)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse -> clientResponse.createException())
                    .bodyToMono(String.class)
                    .timeout(readTimeout())
                    .block();
            return PrometheusMetricsParser.parse(body);
        } catch (RuntimeException ex) {
            log.debug("Agent metrics {} failed: {}", url, rootMessage(ex));
            return null;
        }
    }

    private Duration readTimeout() {
        return Duration.ofMillis(properties.polling().readTimeoutMs());
    }

    private static String agentUrl(String hostname, int port, String path) {
        return "http://" + hostname + ":" + port + path;
    }

    private static String rootMessage(Throwable ex) {
        Throwable root = ex;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        if (root instanceof WebClientRequestException requestException) {
            return requestException.getMessage();
        }
        return root.getMessage() != null ? root.getMessage() : ex.getClass().getSimpleName();
    }

    record ProbeOutcome(boolean success, String error) {
        static ProbeOutcome ok() {
            return new ProbeOutcome(true, null);
        }

        static ProbeOutcome failure(String error) {
            return new ProbeOutcome(false, error);
        }
    }

    public record AgentPollResult(
            boolean healthUp,
            boolean readyUp,
            AgentMetricsResponse metrics,
            String error
    ) {
    }
}
