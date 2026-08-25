package com.ceramiflow.service.ai;

import com.ceramiflow.dto.ExtractedSpecDto;

public interface AIExtractionService {
    ExtractedSpecDto extract(String description);
}