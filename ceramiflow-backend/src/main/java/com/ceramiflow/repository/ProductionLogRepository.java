package com.ceramiflow.repository;

import com.ceramiflow.domain.ProductionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ProductionLogRepository extends JpaRepository<ProductionLog, Long> {
    List<ProductionLog> findByBatchIdOrderByCreatedAtDesc(Long batchId);
}