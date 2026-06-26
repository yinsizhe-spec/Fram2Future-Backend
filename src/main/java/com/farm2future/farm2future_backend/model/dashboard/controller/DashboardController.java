package com.farm2future.farm2future_backend.model.dashboard.controller;

import com.farm2future.farm2future_backend.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DashboardController {
    @GetMapping("/api/dashboard/overview")
    public Result<Map<String,Object>> overview(){
        return Result.success(Map.of(
                "totalFarms", 12,
                "totalTokens", 36,
                "totalTransactions", 58,
                "averageEsgScore", 86
        ));
    }
}
