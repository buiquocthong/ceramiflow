package com.ceramiflow.repository;

import com.ceramiflow.domain.ProductionSpecification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProductionSpecificationRepository extends JpaRepository<ProductionSpecification, Long> {
    Optional<ProductionSpecification> findByOrderId(Long orderId);
}