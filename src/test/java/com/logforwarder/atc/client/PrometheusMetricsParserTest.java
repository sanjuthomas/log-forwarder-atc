package com.logforwarder.atc.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrometheusMetricsParserTest {

    private static final String SAMPLE_METRICS = """
            # HELP log_forwarder_files_watched Number of log files currently being tailed.
            # TYPE log_forwarder_files_watched gauge
            log_forwarder_files_watched{otel_scope_name="github.com/sanjuthomas/log-forwarder"} 1
            # HELP log_forwarder_lines_published_total Total number of log lines published to the configured sink.
            # TYPE log_forwarder_lines_published_total counter
            log_forwarder_lines_published_total{otel_scope_name="github.com/sanjuthomas/log-forwarder"} 68
            # HELP log_forwarder_lines_read_total Total number of log lines read from watched files.
            # TYPE log_forwarder_lines_read_total counter
            log_forwarder_lines_read_total{otel_scope_name="github.com/sanjuthomas/log-forwarder"} 69
            # HELP log_forwarder_lines_replayed_total Total number of log lines re-read after restart because the on-disk watermark lagged behind published data.
            # TYPE log_forwarder_lines_replayed_total counter
            log_forwarder_lines_replayed_total{otel_scope_name="github.com/sanjuthomas/log-forwarder"} 13
            # HELP log_forwarder_pipeline_buffer_depth Current number of line events buffered between watcher and pipeline.
            # TYPE log_forwarder_pipeline_buffer_depth gauge
            log_forwarder_pipeline_buffer_depth{otel_scope_name="github.com/sanjuthomas/log-forwarder"} 0
            # HELP log_forwarder_publish_hibernating Whether the forwarder is in sink hibernate mode after a failed publish batch.
            # TYPE log_forwarder_publish_hibernating gauge
            log_forwarder_publish_hibernating{otel_scope_name="github.com/sanjuthomas/log-forwarder"} 0
            # HELP process_cpu_utilization_ratio CPU used as percent of one core (100 = one core fully busy).
            # TYPE process_cpu_utilization_ratio gauge
            process_cpu_utilization_ratio{otel_scope_name="github.com/sanjuthomas/log-forwarder"} 0.125
            # HELP process_memory_usage_bytes Amount of physical memory used by the forwarder process.
            # TYPE process_memory_usage_bytes gauge
            process_memory_usage_bytes{otel_scope_name="github.com/sanjuthomas/log-forwarder"} 52428800
            """;

    @Test
    void parseExtractsRecommendedForwarderMetrics() {
        var metrics = PrometheusMetricsParser.parse(SAMPLE_METRICS);

        assertNotNull(metrics);
        assertEquals(1L, metrics.filesWatched());
        assertEquals(68L, metrics.linesPublished());
        assertEquals(69L, metrics.linesRead());
        assertEquals(13L, metrics.linesReplayed());
        assertEquals(0L, metrics.pipelineBufferDepth());
        assertFalse(metrics.publishHibernating());
        assertEquals(0.125, metrics.processCpuUtilization());
        assertEquals(52428800L, metrics.processMemoryUsage());
    }

    @Test
    void parseReturnsNullForBlankBody() {
        assertNull(PrometheusMetricsParser.parse(""));
        assertNull(PrometheusMetricsParser.parse(null));
    }

    @Test
    void parseIgnoresHistogramBucketLines() {
        String body = """
                log_forwarder_publish_batch_bytes_bucket{le="500"} 68
                log_forwarder_files_watched 3
                """;

        var metrics = PrometheusMetricsParser.parse(body);

        assertNotNull(metrics);
        assertEquals(3L, metrics.filesWatched());
        assertNull(metrics.linesPublished());
    }

    @Test
    void parseTreatsHibernatingGaugeAsBoolean() {
        String body = "log_forwarder_publish_hibernating 1";

        var metrics = PrometheusMetricsParser.parse(body);

        assertNotNull(metrics);
        assertTrue(metrics.publishHibernating());
    }

    @Test
    void parseExtractsProcessResourceMetricsWithScientificNotation() {
        String body = """
                process_cpu_utilization_ratio{otel_scope_name="github.com/sanjuthomas/log-forwarder"} 0.42671137204925813
                process_memory_usage_bytes{otel_scope_name="github.com/sanjuthomas/log-forwarder"} 2.5493504e+07
                """;

        var metrics = PrometheusMetricsParser.parse(body);

        assertNotNull(metrics);
        assertEquals(0.42671137204925813, metrics.processCpuUtilization());
        assertEquals(25493504L, metrics.processMemoryUsage());
    }
}
