package com.logforwarder.atc.client;

import com.logforwarder.atc.dto.AgentMetricsResponse;

final class PrometheusMetricsParser {

    private static final String FILES_WATCHED = "log_forwarder_files_watched";
    private static final String LINES_PUBLISHED = "log_forwarder_lines_published_total";
    private static final String LINES_READ = "log_forwarder_lines_read_total";
    private static final String PIPELINE_BUFFER_DEPTH = "log_forwarder_pipeline_buffer_depth";
    private static final String PUBLISH_HIBERNATING = "log_forwarder_publish_hibernating";

    private PrometheusMetricsParser() {
    }

    static AgentMetricsResponse parse(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }

        Long filesWatched = extractMetric(body, FILES_WATCHED);
        Long linesPublished = extractMetric(body, LINES_PUBLISHED);
        Long linesRead = extractMetric(body, LINES_READ);
        Long pipelineBufferDepth = extractMetric(body, PIPELINE_BUFFER_DEPTH);
        Boolean publishHibernating = extractBooleanMetric(body, PUBLISH_HIBERNATING);

        if (filesWatched == null
                && linesPublished == null
                && linesRead == null
                && pipelineBufferDepth == null
                && publishHibernating == null) {
            return null;
        }

        return new AgentMetricsResponse(
                filesWatched,
                linesPublished,
                linesRead,
                pipelineBufferDepth,
                publishHibernating
        );
    }

    private static Long extractMetric(String body, String metricName) {
        Double value = extractNumericValue(body, metricName);
        return value == null ? null : Math.round(value);
    }

    private static Boolean extractBooleanMetric(String body, String metricName) {
        Double value = extractNumericValue(body, metricName);
        if (value == null) {
            return null;
        }
        return value >= 1.0;
    }

    private static Double extractNumericValue(String body, String metricName) {
        for (String line : body.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            if (!matchesMetricName(trimmed, metricName)) {
                continue;
            }
            int lastSpace = trimmed.lastIndexOf(' ');
            if (lastSpace < 0 || lastSpace == trimmed.length() - 1) {
                continue;
            }
            try {
                return Double.parseDouble(trimmed.substring(lastSpace + 1).trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static boolean matchesMetricName(String line, String metricName) {
        if (!line.startsWith(metricName)) {
            return false;
        }
        if (line.length() == metricName.length()) {
            return true;
        }
        char next = line.charAt(metricName.length());
        return next == '{' || next == ' ';
    }
}
