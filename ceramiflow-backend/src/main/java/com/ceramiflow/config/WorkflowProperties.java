package com.ceramiflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ceramiflow.workflow")
public record WorkflowProperties(
        double qcPassThreshold, double qcReworkThreshold) {
}
