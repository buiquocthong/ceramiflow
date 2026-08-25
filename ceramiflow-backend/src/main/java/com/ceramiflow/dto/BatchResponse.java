package com.ceramiflow.dto;

import com.ceramiflow.domain.*;
import java.time.*;
import java.util.*;

public record BatchResponse(Long id, String batchCode, Long orderId, String orderCode, Integer quantity,
        BatchStatus status, StageType currentStage, Long version, LocalDateTime startedAt,
        LocalDateTime estimatedCompletionAt, LocalDateTime completedAt, List<WorkflowStepResponse> steps,
        List<QcInspectionResponse> qcInspections) {
}