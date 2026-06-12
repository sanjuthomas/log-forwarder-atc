package com.logforwarder.atc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "fleet_counter")
public class FleetCounter {

    @Id
    private String name;

    @Column(nullable = false)
    private long value;

    protected FleetCounter() {
    }

    public FleetCounter(String name, long value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }
}
