package com.ceramiflow.service.ai;

import com.ceramiflow.dto.ExtractedSpecDto;
import org.springframework.stereotype.Component;
import java.util.regex.*;

@Component
public class RuleBasedExtractionService {
  public ExtractedSpecDto extract(String text) {
    Integer quantity = intMatch(text, "(?i)(?:đơn\\s*)?(\\d+)\\s*(?:bình|sản phẩm|sp|cái|ly|chén|dĩa)", 1);
    if (quantity == null)
      quantity = intMatch(text, "(?i)\\b(\\d{1,5})\\b", 1);
    if (quantity == null)
      quantity = 1;
    Double height = doubleMatch(text, "(?i)(?:cao|height)\\s*(\\d+(?:[.,]\\d+)?)\\s*cm", 1);
    Integer temp = intMatch(text, "(?i)(\\d{3,4})\\s*°?c", 1);
    Integer days = intMatch(text, "(?i)(?:trong|within)\\s*(\\d+)\\s*(?:ngày|days?)", 1);
    String glaze = contains(text, "men lam") ? "Men lam"
        : contains(text, "men rạn") ? "Men rạn" : contains(text, "men ngọc") ? "Men ngọc" : null;
    String pattern = contains(text, "sen") ? "Hoa sen"
        : contains(text, "rồng") ? "Rồng" : contains(text, "hoa") ? "Hoa văn" : null;
    String product = contains(text, "bình") ? "Bình gốm"
        : contains(text, "chén") ? "Chén gốm" : contains(text, "dĩa") ? "Dĩa gốm" : "Sản phẩm gốm";
    double clay = Math.round(quantity * 0.9 * 100.0) / 100.0, glazeKg = Math.round(quantity * 0.125 * 100.0) / 100.0;
    String priority = days != null && days <= 5 ? "URGENT" : days != null && days <= 10 ? "HIGH" : "MEDIUM";
    return new ExtractedSpecDto(product, quantity, null, glaze, pattern, height, null, clay, glazeKg, temp,
        temp != null && temp >= 1200 ? 12.0 : 8.0, days, priority, true,
        "Rule-based fallback estimates must be reviewed before production.", "RULE_BASED");
  }

  private boolean contains(String s, String k) {
    return s.toLowerCase().contains(k.toLowerCase());
  }

  private Integer intMatch(String s, String r, int g) {
    var m = Pattern.compile(r).matcher(s);
    return m.find() ? Integer.valueOf(m.group(g)) : null;
  }

  private Double doubleMatch(String s, String r, int g) {
    var m = Pattern.compile(r).matcher(s);
    return m.find() ? Double.valueOf(m.group(g).replace(',', '.')) : null;
  }
}