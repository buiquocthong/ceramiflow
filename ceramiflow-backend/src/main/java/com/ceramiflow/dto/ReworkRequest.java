package com.ceramiflow.dto;

import com.ceramiflow.domain.StageType;
import jakarta.validation.constraints.*;

public record ReworkRequest(@NotNull StageType targetStage, String operator, String notes) {
}