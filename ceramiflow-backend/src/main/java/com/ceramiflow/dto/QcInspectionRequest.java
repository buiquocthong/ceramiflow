package com.ceramiflow.dto;

import jakarta.validation.constraints.*;

public record QcInspectionRequest(@NotNull @Min(1) Integer quantityInspected, @NotNull @Min(0) Integer quantityPassed,
        @NotNull @Min(0) Integer quantityFailed, String defectType, String severity, @Size(max = 1000) String notes,
        String operator) {
}