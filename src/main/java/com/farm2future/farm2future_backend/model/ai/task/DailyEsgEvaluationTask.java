package com.farm2future.farm2future_backend.model.ai.task;

import com.farm2future.farm2future_backend.model.ai.service.DailyEsgEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyEsgEvaluationTask {

    private final DailyEsgEvaluationService dailyEsgEvaluationService;

    /**
     * 每天凌晨 1 点执行 ESG AI 评级任务。
     *
     * 不写死 farmId。
     * 不写死 AI 特征值。
     * 从 farm_batch + iot_snapshot 真实数据生成 aggregated_features。
     * ESG 分数、评级、解释、Token 建议数量来自 Farm2FutureAI 接口。
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void runDailyEsgEvaluation() {
        log.info("Scheduled daily ESG evaluation task started");
        dailyEsgEvaluationService.runAllFarmPeriods();
        log.info("Scheduled daily ESG evaluation task finished");
    }
}