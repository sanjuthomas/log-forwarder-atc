package com.logforwarder.atc.repository;

import com.logforwarder.atc.entity.LogForwarderInstance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LogForwarderInstanceRepository extends JpaRepository<LogForwarderInstance, UUID> {

    Optional<LogForwarderInstance> findByHostnameAndPort(String hostname, int port);
}
