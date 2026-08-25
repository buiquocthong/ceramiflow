package com.ceramiflow.dto;

public record ExtractedSpecDto(String productType, Integer quantity, String clayType, String glazeType,
        String patternDescription, Double heightCm, Double widthCm, Double estimatedClayKg, Double estimatedGlazeKg,
        Integer firingTemperatureC, Double estimatedFiringHours, Integer deadlineDays, String priority,
        boolean needsReview, String reviewNote, String source) {
}