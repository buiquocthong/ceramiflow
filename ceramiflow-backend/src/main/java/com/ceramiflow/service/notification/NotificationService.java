package com.ceramiflow.service.notification;

import com.ceramiflow.domain.*;

public interface NotificationService {
    void enqueue(ProductionBatch batch, NotificationSeverity severity, String message);
}