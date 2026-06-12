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
        uniqueConstraints = @UniqueConstraint(
                name = "uq_instance_hostname_port",
                columnNames = {"hostname", "port"}
        )
)
public class LogForwarderInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String hostname;

    @Column(name = "process_id", nullable = false)
    private long processId;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(nullable = false)
    private int port;

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
            long processId,
            Instant timestamp,
            int port,
            Instant registeredAt
    ) {
        LogForwarderInstance instance = new LogForwarderInstance();
        instance.hostname = hostname;
        instance.processId = processId;
        instance.timestamp = timestamp;
        instance.port = port;
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

    public long getProcessId() {
        return processId;
    }

    public void setProcessId(long processId) {
        this.processId = processId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
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
