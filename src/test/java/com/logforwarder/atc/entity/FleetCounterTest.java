package com.logforwarder.atc.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FleetCounterTest {

    @Test
    void storesNameAndValue() {
        FleetCounter counter = new FleetCounter("deregistered_total", 7L);

        assertThat(counter.getName()).isEqualTo("deregistered_total");
        assertThat(counter.getValue()).isEqualTo(7L);

        counter.setValue(8L);

        assertThat(counter.getValue()).isEqualTo(8L);
    }
}
