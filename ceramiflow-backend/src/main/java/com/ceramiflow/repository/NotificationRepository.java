package com.ceramiflow.repository;

import com.ceramiflow.domain.*;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @EntityGraph(attributePaths = "batch")
    List<Notification> findTop20ByStatusOrderByCreatedAtAsc(NotificationStatus status);

    List<Notification> findByBatchIdOrderByCreatedAtDesc(Long batchId);
}
