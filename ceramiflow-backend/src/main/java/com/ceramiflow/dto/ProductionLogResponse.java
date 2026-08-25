package com.ceramiflow.dto;

import java.time.*;

public record ProductionLogResponse(Long id, String eventType, String fromStatus, String toStatus, String message,
        String metadata, String createdBy, LocalDateTime createdAt) {
}