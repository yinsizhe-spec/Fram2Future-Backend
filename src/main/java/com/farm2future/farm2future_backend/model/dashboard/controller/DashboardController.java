package com.farm2future.farm2future_backend.model.dashboard.controller;

import com.farm2future.farm2future_backend.common.result.Result;
import com.farm2future.farm2future_backend.model.dashboard.dto.DashboardOverviewDTO;
import com.farm2future.farm2future_backend.model.dashboard.service.DashBoardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DashboardController {
    private final DashBoardService dashBoardService;

    public DashboardController(DashBoardService dashBoardService) {
        this.dashBoardService = dashBoardService;
    }

    @GetMapping("/api/dashboard/overview")
    public Result<DashboardOverviewDTO> overview(){
        return Result.success(dashBoardService.getOverview());
    }
}
