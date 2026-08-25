package com.ceramiflow.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity
@Table(name = "qc_inspections")
@Getter
@Setter
@NoArgsConstructor
public class QcInspection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id", nullable = false)
    private ProductionBatch batch;
    @Column(name = "quantity_inspected", nullable = false)
    private Integer quantityInspected;
    @Column(name = "quantity_passed", nullable = false)
    private Integer quantityPassed;
    @Column(name = "quantity_failed", nullable = false)
    private Integer quantityFailed;
    @Column(name = "defect_type")
    private String defectType;
    @Column(length = 20)
    private String severity;
    @Column(name = "defect_rate", nullable = false)
    private Double defectRate;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QcDecision decision;
    @Column(length = 1000)
    private String notes;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}