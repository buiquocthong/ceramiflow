package com.ceramiflow.controller;

import com.ceramiflow.domain.StageType;
import com.ceramiflow.dto.BatchResponse;
import com.ceramiflow.exception.BusinessException;
import com.ceramiflow.service.telegram.TelegramBotClient;
import com.ceramiflow.service.telegram.TelegramStageText;
import com.ceramiflow.service.workflow.ProductionBatchService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/integrations/telegram")
@Slf4j
public class TelegramWebhookController {
    private final TelegramBotClient telegram;
    private final ProductionBatchService batches;

    public TelegramWebhookController(TelegramBotClient telegram, ProductionBatchService batches) {
        this.telegram = telegram;
        this.batches = batches;
    }

    @GetMapping("/webhook-info")
    public ResponseEntity<?> webhookInfo() {
        try {
            return ResponseEntity.ok(telegram.getWebhookInfo());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("ok", false, "message", e.getMessage() == null ? "Không đọc được webhook info" : e.getMessage()));
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> webhook(
            @RequestHeader(value = "X-Telegram-Bot-Api-Secret-Token", required = false) String secret,
            @RequestBody JsonNode update) {

        if (!telegram.isWebhookSecretValid(secret)) {
            log.warn("Telegram webhook bị từ chối: secret token không hợp lệ hoặc không khớp cấu hình.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("ok", false));
        }

        JsonNode callback = update.path("callback_query");
        if (callback.isMissingNode() || callback.isNull()) {
            return ResponseEntity.ok(Map.of("ok", true));
        }

        String callbackId = callback.path("id").asText("");
        String data = callback.path("data").asText("");
        log.info("Nhận Telegram callback: data={}, callbackIdPresent={}", data, !callbackId.isBlank());
        String chatId = callback.path("message").path("chat").path("id").asText(null);
        Long messageId = callback.path("message").path("message_id").canConvertToLong()
                ? callback.path("message").path("message_id").asLong()
                : null;

        if (!data.startsWith("cf_complete|")) {
            telegram.answerCallback(callbackId, "Thao tác không hợp lệ.", true);
            return ResponseEntity.ok(Map.of("ok", true));
        }

        try {
            String[] parts = data.split("\\|");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid callback payload");
            }

            Long batchId = Long.valueOf(parts[1]);
            StageType expectedStage = StageType.valueOf(parts[2]);
            String actor = telegramActor(callback.path("from"));
            log.info("Xử lý xác nhận Telegram: batchId={}, expectedStage={}, actor={}", batchId, expectedStage, actor);

            BatchResponse response = batches.completeCurrentStepFromTelegram(batchId, expectedStage, actor);
            telegram.removeInlineKeyboard(chatId, messageId);

            String answer = response.currentStage() == StageType.COMPLETED
                    ? "Đã xác nhận. Mẻ gốm đã hoàn thành toàn bộ quy trình."
                    : "Đã xác nhận hoàn thành " + TelegramStageText.label(expectedStage)
                            + ". Mẻ đã chuyển sang " + TelegramStageText.label(response.currentStage()) + ".";
            telegram.answerCallback(callbackId, answer, false);
            log.info("Đã xử lý Telegram callback thành công: batchId={}, {} -> {}",
                    batchId, expectedStage, response.currentStage());
        } catch (BusinessException e) {
            telegram.removeInlineKeyboard(chatId, messageId);
            telegram.answerCallback(callbackId, friendlyBusinessError(e.getMessage()), true);
        } catch (Exception e) {
            log.warn("Không xử lý được callback Telegram: {}", e.getMessage());
            telegram.answerCallback(callbackId, "Không thể xử lý thao tác lúc này. Vui lòng thử lại hoặc kiểm tra trên hệ thống.", true);
        }

        return ResponseEntity.ok(Map.of("ok", true));
    }

    private String telegramActor(JsonNode from) {
        String username = from.path("username").asText("").trim();
        if (!username.isBlank()) {
            return "@" + username + " (Telegram)";
        }
        String firstName = from.path("first_name").asText("").trim();
        String lastName = from.path("last_name").asText("").trim();
        String name = (firstName + " " + lastName).trim();
        return name.isBlank() ? "Người dùng Telegram" : name + " (Telegram)";
    }

    private String friendlyBusinessError(String original) {
        if (original == null) {
            return "Không thể xác nhận công đoạn này.";
        }
        if (original.contains("stage has already changed")) {
            return "Công đoạn này đã được cập nhật trước đó. Vui lòng xem trạng thái mới nhất.";
        }
        if (original.contains("QC")) {
            return "Công đoạn QC cần nhập kết quả kiểm định trên hệ thống, không thể xác nhận chỉ bằng nút Telegram.";
        }
        if (original.contains("not active") || original.contains("completed")) {
            return "Mẻ gốm không còn ở trạng thái có thể xác nhận công đoạn.";
        }
        return "Không thể xác nhận công đoạn: " + original;
    }
}
