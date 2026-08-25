package com.ceramiflow.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "production_orders")
@Getter
@Setter
@NoArgsConstructor
public class ProductionOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "order_code", nullable = false, unique = true, length = 40)
    private String orderCode;
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "raw_description", nullable = false, columnDefinition = "LONGTEXT")
    private String rawDescription;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.CREATED;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PriorityLevel priority = PriorityLevel.MEDIUM;
    @Column(nullable = false)
    private Integer quantity = 1;
    private LocalDate deadline;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}