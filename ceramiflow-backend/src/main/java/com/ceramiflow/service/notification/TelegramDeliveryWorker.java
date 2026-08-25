package com.ceramiflow.service.notification;

import com.ceramiflow.domain.*;
import com.ceramiflow.repository.NotificationRepository;
import com.ceramiflow.service.telegram.TelegramBotClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class TelegramDeliveryWorker {
    private final NotificationRepository repo;
    private final TelegramBotClient telegram;

    public TelegramDeliveryWorker(NotificationRepository repo, TelegramBotClient telegram) {
        this.repo = repo;
        this.telegram = telegram;
    }

    @Scheduled(fixedDelay = 5000)
    public void process() {
        repo.findTop20ByStatusOrderByCreatedAtAsc(NotificationStatus.PENDING).forEach(n -> {
            try {
                deliver(n);
            } catch (Exception e) {
                log.warn("Gửi Telegram thất bại notificationId={}: {}", n.getId(), e.getMessage());
            }
        });
        repo.findTop20ByStatusOrderByCreatedAtAsc(NotificationStatus.FAILED).stream()
                .filter(n -> n.getAttemptCount() < 3)
                .forEach(n -> {
                    try {
                        deliver(n);
                    } catch (Exception ignored) {
                    }
                });
    }

    @Transactional
    public void deliver(Notification n) {
        n.setAttemptCount(n.getAttemptCount() + 1);

        if (!telegram.isConfigured()) {
            n.setStatus(NotificationStatus.SKIPPED);
            n.setLastError("Telegram chưa được bật hoặc thiếu cấu hình bot token/chat id");
            repo.save(n);
            return;
        }

        try {
            telegram.sendNotification(n);
            n.setStatus(NotificationStatus.SENT);
            n.setSentAt(LocalDateTime.now());
            n.setLastError(null);
            repo.save(n);
            log.info("[Telegram SENT] notificationId={} batchId={}", n.getId(),
                    n.getBatch() == null ? null : n.getBatch().getId());
        } catch (Exception e) {
            n.setStatus(NotificationStatus.FAILED);
            n.setLastError(e.getMessage());
            repo.save(n);
            throw new RuntimeException(e);
        }
    }
}
