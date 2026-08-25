package com.ceramiflow.dto;

import jakarta.validation.constraints.*;

public record ConfirmOrderRequest(@NotBlank String productType, @NotNull @Min(1) Integer quantity, String clayType,
        String glazeType, String patternDescription, @Positive Double heightCm, @Positive Double widthCm,
        @PositiveOrZero Double estimatedClayKg, @PositiveOrZero Double estimatedGlazeKg,
        @Min(600) @Max(1500) Integer firingTemperatureC, @Positive Double estimatedFiringHours,
        @Min(1) Integer deadlineDays, @Pattern(regexp = "LOW|MEDIUM|HIGH|URGENT") String priority) {
}