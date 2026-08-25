package com.ceramiflow.repository;

import com.ceramiflow.domain.ProductionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, Long> {
    Optional<ProductionOrder> findByOrderCode(String code);
}