package com.ceramiflow.service.workflow;

import com.ceramiflow.domain.*;
import com.ceramiflow.dto.DashboardSummary;
import com.ceramiflow.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class DashboardService {
    private final ProductionOrderRepository o;
    private final ProductionBatchRepository b;
    private final QcInspectionRepository q;

    public DashboardService(ProductionOrderRepository o, ProductionBatchRepository b, QcInspectionRepository q) {
        this.o = o;
        this.b = b;
        this.q = q;
    }

    @Transactional(readOnly = true)
    public DashboardSummary summary() {
        Map<String, Long> stages = new LinkedHashMap<>();
        for (StageType s : StageType.values())
            stages.put(s.name(), 0L);
        b.findAll().forEach(x -> stages.computeIfPresent(x.getCurrentStage().name(), (k, v) -> v + 1));
        return new DashboardSummary(o.count(), b.countByStatus(BatchStatus.ACTIVE),
                b.countByStatus(BatchStatus.COMPLETED), b.countByStatus(BatchStatus.REWORK_REQUIRED),
                q.countByDecision(QcDecision.REWORK_REQUIRED) + q.countByDecision(QcDecision.REJECT), stages);
    }
}