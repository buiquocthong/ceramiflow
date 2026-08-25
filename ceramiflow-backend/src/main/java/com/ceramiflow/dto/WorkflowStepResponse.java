package com.ceramiflow.dto;

import com.ceramiflow.domain.*;
import java.time.*;

public record WorkflowStepResponse(Long id, StageType stepType, StepStatus status, Integer sequence,
        LocalDateTime startedAt, LocalDateTime completedAt, String operator, String notes) {
}