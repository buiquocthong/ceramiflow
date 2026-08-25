package com.ceramiflow.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity
@Table(name = "workflow_steps", uniqueConstraints = @UniqueConstraint(name = "uk_batch_step_sequence", columnNames = {
        "batch_id", "sequence_no" }))
@Getter
@Setter
@NoArgsConstructor
public class WorkflowStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id", nullable = false)
    private ProductionBatch batch;
    @Enumerated(EnumType.STRING)
    @Column(name = "step_type", nullable = false, length = 30)
    private StageType stepType;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StepStatus status = StepStatus.PENDING;
    @Column(name = "sequence_no", nullable = false)
    private Integer sequence;
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    @Column(length = 120)
    private String operator;
    @Column(length = 1000)
    private String notes;
}