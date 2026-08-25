package com.ceramiflow.controller;

import com.ceramiflow.dto.*;
import com.ceramiflow.service.workflow.ProductionOrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final ProductionOrderService service;

    public OrderController(ProductionOrderService s) {
        service = s;
    }

    @PostMapping
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest r) {
        return service.create(r);
    }

    @PostMapping("/{id}/analyze")
    public OrderResponse analyze(@PathVariable Long id) {
        return service.analyze(id);
    }

    @PostMapping("/{id}/confirm")
    public OrderResponse confirm(@PathVariable Long id, @Valid @RequestBody ConfirmOrderRequest r) {
        return service.confirm(id, r);
    }

    @GetMapping
    public List<OrderResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public OrderResponse find(@PathVariable Long id) {
        return service.find(id);
    }
}
