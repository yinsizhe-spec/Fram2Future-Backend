package com.farm2future.farm2future_backend.model.fram.controller;

import com.farm2future.farm2future_backend.model.fram.service.EsgDailyStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/statistics/esg")
@RequiredArgsConstructor
public class EsgStatisticsController {

    private final EsgDailyStatisticsService esgDailyStatisticsService;

    /**
     * 手动统计 ESG 数据
     *
     * 不传 period：统计当前月份
     * 传 period：统计指定月份
     *
     * 示例：
     * POST /api/statistics/esg/run
     * POST /api/statistics/esg/run?period=2026-07
     */
    @PostMapping("/run")
    public String run(
            @RequestParam(required = false) String period
    ) {
        YearMonth yearMonth = period == null || period.isBlank()
                ? YearMonth.now()
                : YearMonth.parse(period);

        esgDailyStatisticsService.calculateMonthlyEsgScore(yearMonth);

        return "ESG statistics calculated successfully for period: " + yearMonth;
    }
}