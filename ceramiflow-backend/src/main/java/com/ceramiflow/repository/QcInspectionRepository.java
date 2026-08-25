package com.ceramiflow.repository;

import com.ceramiflow.domain.QcInspection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface QcInspectionRepository extends JpaRepository<QcInspection, Long> {
    List<QcInspection> findByBatchIdOrderByCreatedAtDesc(Long batchId);

    long countByDecision(com.ceramiflow.domain.QcDecision decision);
}