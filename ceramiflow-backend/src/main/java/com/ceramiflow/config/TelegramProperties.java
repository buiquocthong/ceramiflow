package com.ceramiflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ceramiflow.telegram")
public record TelegramProperties(boolean enabled, String botToken, String chatId, String webhookSecret) {
}
