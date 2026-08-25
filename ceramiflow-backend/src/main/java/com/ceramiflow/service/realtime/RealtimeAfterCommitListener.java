package com.ceramiflow.service.realtime;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
public class RealtimeAfterCommitListener {
    private final RealtimeEventPublisher publisher;

    public RealtimeAfterCommitListener(RealtimeEventPublisher publisher) {
        this.publisher = publisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(BatchChangedEvent event) {
        try {
            publisher.publish("batch-updated", event);
        } catch (Exception ex) {
            // Database transaction has already committed. Realtime UI refresh is
            // best-effort and must not make the original REST request look failed.
            log.debug("Không thể đẩy realtime event cho batch {}: {}",
                    event.batchId(), ex.getMessage());
        }
    }
}
