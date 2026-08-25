package com.ceramiflow.service.notification;

import com.ceramiflow.domain.*;
import com.ceramiflow.repository.NotificationRepository;
import org.springframework.stereotype.Service;

@Service
public class PersistentNotificationService implements NotificationService {
    private final NotificationRepository repo;

    public PersistentNotificationService(NotificationRepository r) {
        repo = r;
    }

    public void enqueue(ProductionBatch batch, NotificationSeverity severity, String message) {
        Notification n = new Notification();
        n.setBatch(batch);
        n.setSeverity(severity);
        n.setMessage(message);
        repo.save(n);
    }
}