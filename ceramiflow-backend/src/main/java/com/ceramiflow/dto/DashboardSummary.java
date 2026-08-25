package com.ceramiflow.dto;

import java.util.Map;

public record DashboardSummary(long totalOrders, long activeBatches, long completedBatches, long reworkBatches,
        long qcFailures, Map<String, Long> stageDistribution) {
}