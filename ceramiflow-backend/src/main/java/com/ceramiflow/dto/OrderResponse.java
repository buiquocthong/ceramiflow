package com.ceramiflow.dto;

import com.ceramiflow.domain.*;
import java.time.*;

public record OrderResponse(Long id, String orderCode, String rawDescription, OrderStatus status,
        PriorityLevel priority, Integer quantity, LocalDate deadline, ExtractedSpecDto specification,
        LocalDateTime createdAt, LocalDateTime updatedAt) {
}