package com.logforwarder.atc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "atc")
public record AtcProperties(
        Polling polling,
        Agent agent
) {
    public record Polling(long intervalMs, int connectTimeoutMs, int readTimeoutMs) {
    }

    public record Agent(String healthPath, String readyPath, String metricsPath) {
    }
}
