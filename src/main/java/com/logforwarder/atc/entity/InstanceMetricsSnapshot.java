package com.logforwarder.atc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "instance_metrics_snapshot")
@IdClass(InstanceMetricsSnapshot.MetricsId.class)
public class InstanceMetricsSnapshot {

    @Id
    @Column(nullable = false)
    private Instant time;

    @Id
    @Column(name = "instance_id", nullable = false)
    private UUID instanceId;

    @Column(name = "health_up", nullable = false)
    private boolean healthUp;

    @Column(name = "ready_up", nullable = false)
    private boolean readyUp;

    @Column(name = "files_watched")
    private Long filesWatched;

    @Column(name = "lines_published")
    private Long linesPublished;

    @Column(name = "lines_read")
    private Long linesRead;

    @Column(name = "pipeline_buffer_depth")
    private Long pipelineBufferDepth;

    @Column(name = "publish_hibernating")
    private Boolean publishHibernating;

    @Column(name = "poll_error", length = 512)
    private String pollError;

    protected InstanceMetricsSnapshot() {
    }

    public static InstanceMetricsSnapshot of(
            UUID instanceId,
            Instant time,
            boolean healthUp,
            boolean readyUp,
            Long filesWatched,
            Long linesPublished,
            Long linesRead,
            Long pipelineBufferDepth,
            Boolean publishHibernating,
            String pollError
    ) {
        InstanceMetricsSnapshot snapshot = new InstanceMetricsSnapshot();
        snapshot.instanceId = instanceId;
        snapshot.time = time;
        snapshot.healthUp = healthUp;
        snapshot.readyUp = readyUp;
        snapshot.filesWatched = filesWatched;
        snapshot.linesPublished = linesPublished;
        snapshot.linesRead = linesRead;
        snapshot.pipelineBufferDepth = pipelineBufferDepth;
        snapshot.publishHibernating = publishHibernating;
        snapshot.pollError = pollError;
        return snapshot;
    }

    public Instant getTime() {
        return time;
    }

    public UUID getInstanceId() {
        return instanceId;
    }

    public boolean isHealthUp() {
        return healthUp;
    }

    public boolean isReadyUp() {
        return readyUp;
    }

    public Long getFilesWatched() {
        return filesWatched;
    }

    public Long getLinesPublished() {
        return linesPublished;
    }

    public Long getLinesRead() {
        return linesRead;
    }

    public Long getPipelineBufferDepth() {
        return pipelineBufferDepth;
    }

    public Boolean getPublishHibernating() {
        return publishHibernating;
    }

    public String getPollError() {
        return pollError;
    }

    public static class MetricsId implements Serializable {
        private Instant time;
        private UUID instanceId;

        public MetricsId() {
        }

        public MetricsId(Instant time, UUID instanceId) {
            this.time = time;
            this.instanceId = instanceId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof MetricsId that)) {
                return false;
            }
            return Objects.equals(time, that.time) && Objects.equals(instanceId, that.instanceId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(time, instanceId);
        }
    }
}
