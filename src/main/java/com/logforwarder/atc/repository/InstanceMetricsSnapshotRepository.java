package com.logforwarder.atc.repository;

import com.logforwarder.atc.entity.InstanceMetricsSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InstanceMetricsSnapshotRepository
        extends JpaRepository<InstanceMetricsSnapshot, InstanceMetricsSnapshot.MetricsId> {

    Optional<InstanceMetricsSnapshot> findTopByInstanceIdOrderByTimeDesc(UUID instanceId);

    List<InstanceMetricsSnapshot> findByInstanceIdAndTimeAfterOrderByTimeAsc(
            UUID instanceId,
            Instant since
    );
}
