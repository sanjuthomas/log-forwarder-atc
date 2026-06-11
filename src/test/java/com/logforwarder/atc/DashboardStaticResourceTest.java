package com.logforwarder.atc;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardStaticResourceTest {

    @Test
    void dashboardPageIsBundled() throws Exception {
        try (InputStream input = getClass().getResourceAsStream("/static/index.html")) {
            assertThat(input).isNotNull();
            String html = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            assertThat(html).contains("Log Forwarder ATC");
            assertThat(html).contains("/api/instances");
        }
    }

    @Test
    void dashboardSubscribesToFleetEventsAndRendersMetricPanel() throws Exception {
        try (InputStream input = getClass().getResourceAsStream("/static/index.html")) {
            assertThat(input).isNotNull();
            String html = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            assertThat(html).contains("/api/instances/events");
            assertThat(html).contains("EventSource");
            assertThat(html).contains("renderMetricsPanel");
            assertThat(html).contains("files_watched");
            assertThat(html).contains("lines_published");
            assertThat(html).contains("pipeline_buffer_depth");
            assertThat(html).contains("publish_hibernating");
        }
    }
}
