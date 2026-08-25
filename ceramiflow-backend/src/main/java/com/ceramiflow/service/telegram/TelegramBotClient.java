package com.ceramiflow.service.telegram;

import com.ceramiflow.config.TelegramProperties;
import com.ceramiflow.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
@Slf4j
public class TelegramBotClient {
    private final TelegramProperties props;
    private final WebClient client;

    public TelegramBotClient(TelegramProperties props, WebClient.Builder builder) {
        this.props = props;
        this.client = builder.build();
    }

    public boolean isConfigured() {
        return props.enabled()
                && props.botToken() != null && !props.botToken().isBlank()
                && props.chatId() != null && !props.chatId().isBlank();
    }

    public void sendNotification(Notification notification) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("chat_id", props.chatId());
        body.put("text", notification.getMessage());

        ProductionBatch batch = notification.getBatch();
        if (batch != null && shouldShowCompleteButton(batch)) {
            StageType stage = batch.getCurrentStage();
            Map<String, Object> button = Map.of(
                    "text", TelegramStageText.buttonLabel(stage),
                    "callback_data", callbackData(batch.getId(), stage));
            body.put("reply_markup", Map.of("inline_keyboard", List.of(List.of(button))));
        }

        post("/sendMessage", body);
    }

    public void answerCallback(String callbackQueryId, String text, boolean showAlert) {
        post("/answerCallbackQuery", Map.of(
                "callback_query_id", callbackQueryId,
                "text", text,
                "show_alert", showAlert));
    }

    public void removeInlineKeyboard(String chatId, Long messageId) {
        if (chatId == null || messageId == null) {
            return;
        }
        try {
            post("/editMessageReplyMarkup", Map.of(
                    "chat_id", chatId,
                    "message_id", messageId,
                    "reply_markup", Map.of("inline_keyboard", List.of())));
        } catch (Exception e) {
            log.debug("Không thể xoá nút Telegram cũ: {}", e.getMessage());
        }
    }

    public boolean isWebhookSecretValid(String receivedSecret) {
        String configured = props.webhookSecret();
        if (configured == null || configured.isBlank()) {
            return true;
        }
        return Objects.equals(configured, receivedSecret);
    }

    public static String callbackData(Long batchId, StageType stage) {
        return "cf_complete|" + batchId + "|" + stage.name();
    }

    private boolean shouldShowCompleteButton(ProductionBatch batch) {
        return batch.getStatus() == BatchStatus.ACTIVE
                && batch.getCurrentStage() != StageType.QC
                && batch.getCurrentStage() != StageType.COMPLETED;
    }

    public JsonNode getWebhookInfo() {
        if (props.botToken() == null || props.botToken().isBlank()) {
            throw new IllegalStateException("Telegram bot token chưa được cấu hình");
        }
        return client.get()
                .uri("https://api.telegram.org/bot" + props.botToken() + "/getWebhookInfo")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(10))
                .block();
    }

    private void post(String method, Object body) {
        client.post()
                .uri("https://api.telegram.org/bot" + props.botToken() + method)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.isError(), response ->
                        response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .map(error -> new IllegalStateException(
                                        "Telegram API " + response.statusCode() + ": " + error)))
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(10))
                .block();
    }
}
