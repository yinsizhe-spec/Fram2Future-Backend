package com.farm2future.farm2future_backend.model.fram.task;

import com.farm2future.farm2future_backend.model.fram.service.EsgDailyStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

@Slf4j
@Component
@RequiredArgsConstructor
public class EsgDailyStatisticsTask {

    private final EsgDailyStatisticsService esgDailyStatisticsService;

    /**
     * 每天凌晨 00:10 自动统计当前月份 ESG 数据
     *
     * cron 格式：
     * 秒 分 时 日 月 星期
     */
    @Scheduled(cron = "0 10 0 * * ?")
    public void calculateDailyEsgStatistics() {
        YearMonth currentMonth = YearMonth.now();

        log.info("Start daily ESG statistics task, period={}", currentMonth);

        try {
            esgDailyStatisticsService.calculateMonthlyEsgScore(currentMonth);
            log.info("Daily ESG statistics task finished, period={}", currentMonth);
        } catch (Exception e) {
            log.error("Daily ESG statistics task failed, period={}", currentMonth, e);
        }
    }
}