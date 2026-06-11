package com.logforwarder.atc.entity;

import com.logforwarder.atc.domain.Reachability;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "log_forwarder_instance",
        uniqueConstraints = @UniqueConstraint(name = "uq_instance_hostname_pid", columnNames = {"hostname", "pid"})
)
public class LogForwarderInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String hostname;

    @Column(nullable = false)
    private long pid;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "health_port", nullable = false)
    private int healthPort;

    @Column(name = "ready_port", nullable = false)
    private int readyPort;

    @Column(name = "metrics_port", nullable = false)
    private int metricsPort;

    @Column(name = "registered_at", nullable = false)
    private Instant registeredAt;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Reachability reachability = Reachability.UNKNOWN;

    protected LogForwarderInstance() {
    }

    public static LogForwarderInstance create(
            String hostname,
            long pid,
            Instant startTime,
            int healthPort,
            int readyPort,
            int metricsPort,
            Instant registeredAt
    ) {
        LogForwarderInstance instance = new LogForwarderInstance();
        instance.hostname = hostname;
        instance.pid = pid;
        instance.startTime = startTime;
        instance.healthPort = healthPort;
        instance.readyPort = readyPort;
        instance.metricsPort = metricsPort;
        instance.registeredAt = registeredAt;
        instance.reachability = Reachability.UNKNOWN;
        return instance;
    }

    public UUID getId() {
        return id;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public long getPid() {
        return pid;
    }

    public void setPid(long pid) {
        this.pid = pid;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public int getHealthPort() {
        return healthPort;
    }

    public void setHealthPort(int healthPort) {
        this.healthPort = healthPort;
    }

    public int getReadyPort() {
        return readyPort;
    }

    public void setReadyPort(int readyPort) {
        this.readyPort = readyPort;
    }

    public int getMetricsPort() {
        return metricsPort;
    }

    public void setMetricsPort(int metricsPort) {
        this.metricsPort = metricsPort;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(Instant registeredAt) {
        this.registeredAt = registeredAt;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Instant lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public Reachability getReachability() {
        return reachability;
    }

    public void setReachability(Reachability reachability) {
        this.reachability = reachability;
    }
}
