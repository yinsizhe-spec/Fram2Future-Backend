package com.farm2future.farm2future_backend.model.dashboard.controller;

import com.farm2future.farm2future_backend.model.dashboard.dto.DashboardOverviewResponse;
import com.farm2future.farm2future_backend.model.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/overview")
    public DashboardOverviewResponse getOverview(
            @RequestParam(defaultValue = "All Farms") String farm
    ) {
        return dashboardService.getOverview(farm);
    }
}
