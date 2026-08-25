package com.ceramiflow.service.ai;

import com.ceramiflow.config.AIProperties;
import com.ceramiflow.dto.ExtractedSpecDto;
import com.ceramiflow.exception.AIExtractionException;
import com.fasterxml.jackson.databind.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Duration;
import java.util.*;

@Service
@Slf4j
public class LLMExtractionService implements AIExtractionService {
  private final AIProperties props;
  private final RuleBasedExtractionService fallback;
  private final ObjectMapper mapper;
  private final WebClient client;

  public LLMExtractionService(AIProperties props, RuleBasedExtractionService fallback, ObjectMapper mapper,
      WebClient.Builder builder) {
    this.props = props;
    this.fallback = fallback;
    this.mapper = mapper;
    this.client = builder.baseUrl(props.baseUrl()).build();
  }

  @Override
  public ExtractedSpecDto extract(String description) {
    if (!props.enabled() || props.apiKey() == null || props.apiKey().isBlank()) {
      log.warn("AI disabled/not configured; using rule-based fallback");
      return fallback.extract(description);
    }
    Exception last = null;
    for (int attempt = 1; attempt <= Math.max(1, props.maxRetries()); attempt++) {
      try {
        return call(description);
      } catch (Exception e) {
        last = e;
        log.warn("LLM extraction attempt {}/{} failed: {}", attempt, props.maxRetries(), e.getMessage());
        if (attempt < props.maxRetries())
          sleep(props.retryBackoffMs() * (1L << (attempt - 1)));
      }
    }
    log.warn("Falling back to rule-based extraction due to: {}", last == null ? "unknown" : last.getMessage());
    return fallback.extract(description);
  }

  private ExtractedSpecDto call(String description) {
    String system = """
        You extract ceramic manufacturing specifications. Return JSON only, no markdown. Never invent explicit customer requirements; use null when absent. Estimates are allowed only for estimatedClayKg, estimatedGlazeKg, estimatedFiringHours and must set needsReview=true. Normalize cm, kg, Celsius, hours. Schema: {\"productType\":string,\"quantity\":integer,\"clayType\":string|null,\"glazeType\":string|null,\"patternDescription\":string|null,\"heightCm\":number|null,\"widthCm\":number|null,\"estimatedClayKg\":number|null,\"estimatedGlazeKg\":number|null,\"firingTemperatureC\":integer|null,\"estimatedFiringHours\":number|null,\"deadlineDays\":integer|null,\"priority\":\"LOW|MEDIUM|HIGH|URGENT\",\"needsReview\":boolean,\"reviewNote\":string|null}. Priority should consider deadline and production complexity.
        """;
    Map<String, Object> body = Map.of("model", props.model(), "temperature", 0.1, "response_format",
        Map.of("type", "json_object"), "messages",
        List.of(Map.of("role", "system", "content", system), Map.of("role", "user", "content", description)));
    JsonNode response = client.post().uri("/chat/completions").contentType(MediaType.APPLICATION_JSON)
        .headers(h -> h.setBearerAuth(props.apiKey())).bodyValue(body).retrieve().bodyToMono(JsonNode.class)
        .timeout(Duration.ofSeconds(props.timeoutSeconds())).block();
    if (response == null)
      throw new AIExtractionException("Empty LLM response");
    String content = response.path("choices").path(0).path("message").path("content").asText(null);
    if (content == null)
      throw new AIExtractionException("Missing message content");
    try {
      JsonNode n = mapper.readTree(content);
      return validate(n);
    } catch (Exception e) {
      throw new AIExtractionException("Invalid AI JSON: " + e.getMessage(), e);
    }
  }

  private ExtractedSpecDto validate(JsonNode n) {
    String product = text(n, "productType");
    Integer quantity = integer(n, "quantity");
    if (product == null || product.isBlank())
      throw new AIExtractionException("productType is required");
    if (quantity == null || quantity <= 0)
      throw new AIExtractionException("quantity must be > 0");
    Integer temp = integer(n, "firingTemperatureC");
    boolean review = bool(n, "needsReview", false);
    String note = text(n, "reviewNote");
    if (temp != null && (temp < 600 || temp > 1500)) {
      review = true;
      note = append(note, "Firing temperature outside configured realistic range (600-1500°C).");
    }
    Double clay = num(n, "estimatedClayKg"), glaze = num(n, "estimatedGlazeKg"), hours = num(n, "estimatedFiringHours"),
        h = num(n, "heightCm"), w = num(n, "widthCm");
    Integer days = integer(n, "deadlineDays");
    if (clay != null && clay < 0 || glaze != null && glaze < 0 || hours != null && hours <= 0 || h != null && h <= 0
        || w != null && w <= 0 || days != null && days <= 0)
      throw new AIExtractionException("AI returned invalid numeric business values");
    String p = Optional.ofNullable(text(n, "priority")).orElse("MEDIUM").toUpperCase();
    if (!Set.of("LOW", "MEDIUM", "HIGH", "URGENT").contains(p)) {
      p = "MEDIUM";
      review = true;
      note = append(note, "Priority normalized to MEDIUM.");
    }
    return new ExtractedSpecDto(product, quantity, text(n, "clayType"), text(n, "glazeType"),
        text(n, "patternDescription"), h, w, clay, glaze, temp, hours, days, p, review, note, "LLM");
  }

  private String append(String a, String b) {
    return a == null || a.isBlank() ? b : a + " " + b;
  }

  private String text(JsonNode n, String k) {
    return n.path(k).isNull() || n.path(k).isMissingNode() ? null : n.path(k).asText();
  }

  private Integer integer(JsonNode n, String k) {
    return n.path(k).isNumber() ? n.path(k).asInt() : null;
  }

  private Double num(JsonNode n, String k) {
    return n.path(k).isNumber() ? n.path(k).asDouble() : null;
  }

  private boolean bool(JsonNode n, String k, boolean d) {
    return n.path(k).isBoolean() ? n.path(k).asBoolean() : d;
  }

  private void sleep(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}