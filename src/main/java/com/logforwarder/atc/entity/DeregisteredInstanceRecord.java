package com.logforwarder.atc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "deregistered_instance")
public class DeregisteredInstanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "instance_id", nullable = false)
    private UUID instanceId;

    @Column(nullable = false)
    private String hostname;

    @Column(name = "process_id", nullable = false)
    private long processId;

    @Column(nullable = false)
    private int port;

    @Column(name = "registered_at", nullable = false)
    private Instant registeredAt;

    @Column(name = "deregistered_at", nullable = false)
    private Instant deregisteredAt;

    protected DeregisteredInstanceRecord() {
    }

    public static DeregisteredInstanceRecord from(LogForwarderInstance instance, Instant deregisteredAt) {
        DeregisteredInstanceRecord record = new DeregisteredInstanceRecord();
        record.instanceId = instance.getId();
        record.hostname = instance.getHostname();
        record.processId = instance.getProcessId();
        record.port = instance.getPort();
        record.registeredAt = instance.getRegisteredAt();
        record.deregisteredAt = deregisteredAt;
        return record;
    }

    public UUID getId() {
        return id;
    }

    public UUID getInstanceId() {
        return instanceId;
    }

    public String getHostname() {
        return hostname;
    }

    public long getProcessId() {
        return processId;
    }

    public int getPort() {
        return port;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }

    public Instant getDeregisteredAt() {
        return deregisteredAt;
    }
}
