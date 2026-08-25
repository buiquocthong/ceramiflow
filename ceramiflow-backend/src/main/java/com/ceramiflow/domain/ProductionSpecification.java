package com.ceramiflow.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity
@Table(name = "production_specifications")
@Getter
@Setter
@NoArgsConstructor
public class ProductionSpecification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private ProductionOrder order;
    @Column(name = "product_type", nullable = false)
    private String productType;
    @Column(name = "clay_type")
    private String clayType;
    @Column(name = "glaze_type")
    private String glazeType;
    @Column(name = "pattern_description")
    private String patternDescription;
    @Column(name = "height_cm")
    private Double heightCm;
    @Column(name = "width_cm")
    private Double widthCm;
    @Column(name = "estimated_clay_kg")
    private Double estimatedClayKg;
    @Column(name = "estimated_glaze_kg")
    private Double estimatedGlazeKg;
    @Column(name = "firing_temperature_c")
    private Integer firingTemperatureC;
    @Column(name = "estimated_firing_hours")
    private Double estimatedFiringHours;
    @Column(name = "deadline_days")
    private Integer deadlineDays;
    @Column(name = "needs_review", nullable = false)
    private boolean needsReview;
    @Column(name = "review_note", length = 1000)
    private String reviewNote;
    @Column(name = "source", nullable = false, length = 20)
    private String source;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}