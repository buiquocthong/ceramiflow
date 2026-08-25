package com.ceramiflow.repository;

import com.ceramiflow.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ProductionBatchRepository extends JpaRepository<ProductionBatch, Long> {
    Optional<ProductionBatch> findByBatchCode(String code);

    Optional<ProductionBatch> findByOrderId(Long orderId);

    long countByStatus(BatchStatus status);
}