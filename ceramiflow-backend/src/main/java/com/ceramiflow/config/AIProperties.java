package com.ceramiflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ceramiflow.ai")
public record AIProperties(
        boolean enabled,
        String baseUrl,
        String apiKey,
        String model,
        int maxRetries,
        int timeoutSeconds,
        long retryBackoffMs) {
}
