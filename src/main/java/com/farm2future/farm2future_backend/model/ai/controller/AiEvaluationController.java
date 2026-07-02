package com.farm2future.farm2future_backend.model.ai.controller;

import com.farm2future.farm2future_backend.model.ai.service.DailyEsgEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai/evaluation")
@RequiredArgsConstructor
public class AiEvaluationController {

    private final DailyEsgEvaluationService dailyEsgEvaluationService;

    /**
     * 手动触发全部农场、全部月份的 AI ESG 评级。
     *
     * 示例：
     * POST /api/ai/evaluation/run
     */
    @PostMapping("/run")
    public ResponseEntity<Map<String, Object>> runAll() {
        dailyEsgEvaluationService.runAllFarmPeriods();

        return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "AI ESG evaluation task executed successfully",
                "data", Map.of()
        ));
    }

    /**
     * 手动触发某个农场某个月份的 AI ESG 评级。
     *
     * 示例：
     * POST /api/ai/evaluation/run-one?farmId=farm_001&period=2026-06
     */
    @PostMapping("/run-one")
    public ResponseEntity<Map<String, Object>> runOne(
            @RequestParam String farmId,
            @RequestParam String period
    ) {
        dailyEsgEvaluationService.runOneFarmPeriod(farmId, period);

        return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "AI ESG evaluation task executed successfully",
                "data", Map.of(
                        "farmId", farmId,
                        "period", period
                )
        ));
    }
}