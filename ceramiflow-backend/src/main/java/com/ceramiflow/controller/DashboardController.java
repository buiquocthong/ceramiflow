package com.ceramiflow.controller;

import com.ceramiflow.dto.DashboardSummary;
import com.ceramiflow.service.workflow.DashboardService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService s;

    public DashboardController(DashboardService s) {
        this.s = s;
    }

    @GetMapping("/summary")
    public DashboardSummary summary() {
        return s.summary();
    }
}