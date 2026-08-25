package com.ceramiflow.service.workflow;

import com.ceramiflow.domain.*;
import com.ceramiflow.repository.ProductionLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final ProductionLogRepository repo;

    public AuditService(ProductionLogRepository r) {
        repo = r;
    }

    public void log(ProductionBatch batch, String type, String from, String to, String message, String metadata,
            String actor) {
        ProductionLog l = new ProductionLog();
        l.setBatch(batch);
        l.setEventType(type);
        l.setFromStatus(from);
        l.setToStatus(to);
        l.setMessage(message);
        l.setMetadata(metadata);
        l.setCreatedBy(actor);
        repo.save(l);
    }
}
