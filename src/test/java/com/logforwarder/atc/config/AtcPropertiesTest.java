package com.logforwarder.atc.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

class AtcPropertiesTest {

    @SpringBootTest(classes = AtcPropertiesTest.TestConfig.class)
    @TestPropertySource(properties = {
            "atc.polling.interval-ms=10000",
            "atc.polling.connect-timeout-ms=1500",
            "atc.polling.read-timeout-ms=2500",
            "atc.agent.health-path=/health",
            "atc.agent.ready-path=/ready",
            "atc.agent.metrics-path=/metrics"
    })
    static class Bindings {

        @Autowired
        private AtcProperties properties;

        @Test
        void bindsAtcConfigurationProperties() {
            assertThat(properties.polling().intervalMs()).isEqualTo(10_000L);
            assertThat(properties.polling().connectTimeoutMs()).isEqualTo(1500);
            assertThat(properties.polling().readTimeoutMs()).isEqualTo(2500);
            assertThat(properties.agent().healthPath()).isEqualTo("/health");
            assertThat(properties.agent().readyPath()).isEqualTo("/ready");
            assertThat(properties.agent().metricsPath()).isEqualTo("/metrics");
        }
    }

    @SpringBootTest(classes = {WebClientConfig.class})
    static class WebClientConfigTest {

        @Autowired
        private org.springframework.web.reactive.function.client.WebClient webClientBean;

        @Test
        void createsWebClientBean() {
            assertThat(webClientBean).isNotNull();
        }
    }

    @EnableConfigurationProperties(AtcProperties.class)
    static class TestConfig {
    }
}
