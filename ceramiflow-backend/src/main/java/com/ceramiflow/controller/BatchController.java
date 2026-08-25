package com.ceramiflow.controller;

import com.ceramiflow.dto.*;
import com.ceramiflow.service.workflow.ProductionBatchService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/batches")
public class BatchController {
    private final ProductionBatchService service;

    public BatchController(ProductionBatchService s) {
        service = s;
    }

    @PostMapping("/from-order/{orderId}")
    public BatchResponse create(@PathVariable Long orderId, @RequestParam(defaultValue = "system") String actor) {
        return service.createFromOrder(orderId, actor);
    }

    @GetMapping
    public List<BatchResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public BatchResponse find(@PathVariable Long id) {
        return service.find(id);
    }

    @PostMapping("/{id}/steps/complete")
    public BatchResponse complete(@PathVariable Long id, @RequestBody(required = false) BatchActionRequest r) {
        return service.completeCurrentStep(id, r == null ? new BatchActionRequest("system", null) : r);
    }

    @PostMapping("/{id}/qc")
    public QcInspectionResponse qc(@PathVariable Long id, @Valid @RequestBody QcInspectionRequest r) {
        return service.inspect(id, r);
    }

    @PostMapping("/{id}/rework")
    public BatchResponse rework(@PathVariable Long id, @Valid @RequestBody ReworkRequest r) {
        return service.rework(id, r);
    }

    @GetMapping("/{id}/logs")
    public List<ProductionLogResponse> logs(@PathVariable Long id) {
        return service.logs(id);
    }

    @GetMapping("/{id}/notifications")
    public List<NotificationResponse> notifications(@PathVariable Long id) {
        return service.notifications(id);
    }
}
