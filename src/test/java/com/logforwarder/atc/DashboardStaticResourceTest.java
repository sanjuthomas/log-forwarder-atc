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
            assertThat(html).contains("summaryDeregistered");
            assertThat(html).contains("/api/instances/stats");
            assertThat(html).contains("EventSource");
            assertThat(html).contains("th-with-info");
            assertThat(html).contains("metric-info-btn");
            assertThat(html).contains("Forwarder metrics");
            assertThat(html).contains("metricsHelpModal");
            assertThat(html).contains("openMetricsHelp");
            assertThat(html).contains("log_forwarder_files_watched");
            assertThat(html).contains("lines_replayed");
            assertThat(html).contains("totalLinesIngested");
            assertThat(html).contains("lines_published");
            assertThat(html).contains("pipeline_buffer_depth");
            assertThat(html).contains("publish_hibernating");
            assertThat(html).contains("process_cpu_utilization");
            assertThat(html).contains("process_memory_usage");
        }
    }

    @Test
    void dashboardSummaryCardsFilterAgentTable() throws Exception {
        try (InputStream input = getClass().getResourceAsStream("/static/index.html")) {
            assertThat(input).isNotNull();
            String html = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            assertThat(html).contains("summary-card filterable");
            assertThat(html).contains("data-filter=\"UNREACHABLE\"");
            assertThat(html).contains("data-filter=\"deregistered\"");
            assertThat(html).contains("/api/instances/deregistered");
            assertThat(html).contains("setActiveFilter");
            assertThat(html).contains("renderDeregisteredTable");
            assertThat(html).contains("filterInstances");
        }
    }
}
