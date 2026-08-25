package com.ceramiflow.service.realtime;

public record BatchChangedEvent(Long batchId, String batchCode, String previousStatus, String newStatus,
        String eventType, String message) {
}