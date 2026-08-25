package com.ceramiflow.service.workflow;

import com.ceramiflow.config.WorkflowProperties;
import com.ceramiflow.domain.QcDecision;
import org.springframework.stereotype.Component;

@Component
public class QcPolicy {
    private final WorkflowProperties p;

    public QcPolicy(WorkflowProperties p) {
        this.p = p;
    }

    public QcDecision decide(double defectRate) {
        return defectRate < p.qcPassThreshold() ? QcDecision.PASS
                : defectRate < p.qcReworkThreshold() ? QcDecision.REWORK_REQUIRED : QcDecision.REJECT;
    }
}
