package com.ceramiflow.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "production_logs")
@Getter
@Setter
@NoArgsConstructor
public class ProductionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private ProductionBatch batch;
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;
    @Column(name = "from_status", length = 40)
    private String fromStatus;
    @Column(name = "to_status", length = 40)
    private String toStatus;
    @Column(nullable = false, length = 1000)
    private String message;
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(columnDefinition = "LONGTEXT")
    private String metadata;
    @Column(name = "created_by", length = 120)
    private String createdBy;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}