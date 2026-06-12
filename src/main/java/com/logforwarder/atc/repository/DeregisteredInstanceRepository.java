package com.logforwarder.atc.repository;

import com.logforwarder.atc.entity.DeregisteredInstanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DeregisteredInstanceRepository extends JpaRepository<DeregisteredInstanceRecord, UUID> {

    List<DeregisteredInstanceRecord> findAllByOrderByDeregisteredAtDesc();
}
