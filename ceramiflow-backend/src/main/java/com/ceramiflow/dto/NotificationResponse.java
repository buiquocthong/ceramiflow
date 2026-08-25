package com.ceramiflow.dto;

import com.ceramiflow.domain.*;
import java.time.*;

public record NotificationResponse(
        Long id,
        String channel,
        NotificationSeverity severity,
        String message,
        NotificationStatus status,
        Integer attemptCount,
        String lastError,
        LocalDateTime sentAt,
        LocalDateTime createdAt) {
}