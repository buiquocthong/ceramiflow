package com.ceramiflow.service.workflow;

import com.ceramiflow.domain.*;
import com.ceramiflow.exception.BusinessException;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class WorkflowStateMachine {
    private static final Set<StageType> REWORK_TARGETS = EnumSet.of(StageType.FORMING, StageType.DRYING_REPAIR,
            StageType.PAINTING, StageType.GLAZING, StageType.READY_FOR_KILN, StageType.FIRING);

    public StageType next(StageType current) {
        if (current == StageType.COMPLETED)
            throw new BusinessException("Batch is already completed");
        return current.next();
    }

    public void validateAdvance(StageType current, StageType target) {
        StageType expected = next(current);
        if (target != expected)
            throw new BusinessException(
                    "Invalid workflow transition: " + current + " -> " + target + ". Expected " + expected);
    }

    public void validateReworkTarget(StageType target) {
        if (!REWORK_TARGETS.contains(target))
            throw new BusinessException("Invalid rework target: " + target);
    }
}