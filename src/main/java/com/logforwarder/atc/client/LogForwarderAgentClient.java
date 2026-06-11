package com.logforwarder.atc.client;

import com.logforwarder.atc.config.AtcProperties;
import com.logforwarder.atc.dto.AgentMetricsResponse;
import com.logforwarder.atc.entity.LogForwarderInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

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
        boolean healthUp = probeEndpoint(baseHost, port, properties.agent().healthPath());
        boolean readyUp = probeEndpoint(baseHost, port, properties.agent().readyPath());
        AgentMetricsResponse metrics = fetchMetrics(baseHost, port);

        String error = null;
        if (!healthUp && !readyUp && metrics == null) {
            error = "All agent probes failed";
        }

        return new AgentPollResult(healthUp, readyUp, metrics, error);
    }

    private boolean probeEndpoint(String hostname, int port, String path) {
        String url = agentUrl(hostname, port, path);
        try {
            webClient.get()
                    .uri(url)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> response.createException())
                    .toBodilessEntity()
                    .timeout(readTimeout())
                    .block();
            return true;
        } catch (WebClientResponseException ex) {
            log.debug("Agent probe {} returned HTTP {}: {}", url, ex.getStatusCode().value(), ex.getMessage());
            return false;
        } catch (RuntimeException ex) {
            log.debug("Agent probe {} failed: {}", url, rootMessage(ex));
            return false;
        }
    }

    private AgentMetricsResponse fetchMetrics(String hostname, int port) {
        String url = agentUrl(hostname, port, properties.agent().metricsPath());
        try {
            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> response.createException())
                    .bodyToMono(AgentMetricsResponse.class)
                    .timeout(readTimeout())
                    .block();
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

    public record AgentPollResult(
            boolean healthUp,
            boolean readyUp,
            AgentMetricsResponse metrics,
            String error
    ) {
    }
}
