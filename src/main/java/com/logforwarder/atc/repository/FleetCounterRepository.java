package com.logforwarder.atc.repository;

import com.logforwarder.atc.entity.FleetCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface FleetCounterRepository extends JpaRepository<FleetCounter, String> {

    @Modifying
    @Query("UPDATE FleetCounter c SET c.value = c.value + 1 WHERE c.name = :name")
    int increment(String name);
}
