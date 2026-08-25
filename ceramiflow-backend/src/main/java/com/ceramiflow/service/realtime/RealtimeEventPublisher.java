package com.ceramiflow.service.realtime;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class RealtimeEventPublisher {
    private static final long SSE_TIMEOUT_MS = 30 * 60 * 1000L;

    private final CopyOnWriteArrayList<SseEmitter> clients = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        clients.add(emitter);

        emitter.onCompletion(() -> remove(emitter, "completed"));
        emitter.onTimeout(() -> {
            remove(emitter, "timeout");
            safeComplete(emitter);
        });
        emitter.onError(error -> remove(emitter, "error: " + safeMessage(error)));

        // Initial event confirms that the SSE channel is usable and lets us discard
        // a socket that the browser already closed before the first business event.
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("CeramiFlow realtime connected"));
        } catch (Exception ex) {
            remove(emitter, "initial send failed: " + safeMessage(ex));
            safeComplete(emitter);
        }

        return emitter;
    }

    /**
     * Realtime delivery is best-effort. A browser may refresh, navigate away or lose
     * its network connection at any time. Such a disconnect must never turn a
     * successful manufacturing state transition into HTTP 500.
     */
    public void publish(String name, Object payload) {
        for (SseEmitter emitter : clients) {
            try {
                emitter.send(SseEmitter.event().name(name).data(payload));
            } catch (Exception ex) {
                remove(emitter, "send failed: " + safeMessage(ex));
                safeComplete(emitter);
            }
        }
    }

    private void remove(SseEmitter emitter, String reason) {
        if (clients.remove(emitter)) {
            log.debug("Đã gỡ một kết nối SSE khỏi danh sách ({})", reason);
        }
    }

    private void safeComplete(SseEmitter emitter) {
        try {
            emitter.complete();
        } catch (Exception ignored) {
            // Socket is already unusable; there is nothing else to send to it.
        }
    }

    private String safeMessage(Throwable error) {
        if (error == null || error.getMessage() == null) {
            return error == null ? "unknown" : error.getClass().getSimpleName();
        }
        return error.getMessage();
    }
}
