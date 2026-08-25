package com.ceramiflow.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity
@Table(name = "production_batches")
@Getter
@Setter
@NoArgsConstructor
public class ProductionBatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version
    private Long version;
    @Column(name = "batch_code", nullable = false, unique = true, length = 40)
    private String batchCode;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private ProductionOrder order;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BatchStatus status = BatchStatus.ACTIVE;
    @Enumerated(EnumType.STRING)
    @Column(name = "current_stage", nullable = false, length = 30)
    private StageType currentStage = StageType.FORMING;
    @Column(nullable = false)
    private Integer quantity;
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;
    @Column(name = "estimated_completion_at")
    private LocalDateTime estimatedCompletionAt;
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    void prePersist() {
        if (startedAt == null)
            startedAt = LocalDateTime.now();
    }
}