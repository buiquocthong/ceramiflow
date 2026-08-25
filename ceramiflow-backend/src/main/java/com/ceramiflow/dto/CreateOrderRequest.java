package com.ceramiflow.dto;

import jakarta.validation.constraints.*;

public record CreateOrderRequest(@NotBlank @Size(max = 5000) String description) {
}