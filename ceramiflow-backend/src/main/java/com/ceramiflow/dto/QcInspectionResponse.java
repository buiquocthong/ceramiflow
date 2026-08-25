package com.ceramiflow.dto;

import com.ceramiflow.domain.QcDecision;
import java.time.*;

public record QcInspectionResponse(Long id, Integer quantityInspected, Integer quantityPassed, Integer quantityFailed,
        String defectType, String severity, Double defectRate, QcDecision decision, String notes,
        LocalDateTime createdAt) {
}