package com.farm2future.farm2future_backend.model.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.farm2future.farm2future_backend.model.ai.client.Farm2FutureAiClient;
import com.farm2future.farm2future_backend.model.ai.dto.AiEvaluateRequest;
import com.farm2future.farm2future_backend.model.ai.dto.AiEvaluateResponse;
import com.farm2future.farm2future_backend.model.fram.entity.EsgScore;
import com.farm2future.farm2future_backend.model.fram.entity.FarmBatch;
import com.farm2future.farm2future_backend.model.fram.mapper.EsgScoreMapper;
import com.farm2future.farm2future_backend.model.fram.mapper.FarmBatchMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyEsgEvaluationService {

    private final FarmBatchMapper farmBatchMapper;
    private final EsgScoreMapper esgScoreMapper;
    private final Farm2FutureAiClient farm2FutureAiClient;
    private final AiFeatureAggregationService aiFeatureAggregationService;

    public void runAllFarmPeriods() {
        log.info("Start daily ESG AI evaluation service");

        List<FarmBatch> batches = farmBatchMapper.selectList(
                new LambdaQueryWrapper<FarmBatch>()
                        .eq(FarmBatch::getDeleted, 0)
        );

        if (batches == null || batches.isEmpty()) {
            log.warn("No farm_batch data found, skip ESG AI evaluation");
            return;
        }

        List<String> farmPeriodKeys = batches.stream()
                .filter(batch -> StringUtils.hasText(batch.getFarmId()))
                .filter(batch -> batch.getBatchDate() != null)
                .map(batch -> batch.getFarmId() + "|" + toYearMonth(batch.getBatchDate()))
                .distinct()
                .toList();

        for (String key : farmPeriodKeys) {
            String[] parts = key.split("\\|");

            if (parts.length != 2) {
                continue;
            }

            runOneFarmPeriod(parts[0], parts[1]);
        }

        log.info("Daily ESG AI evaluation service finished");
    }

    public void runOneFarmPeriod(String farmId, String period) {
        if (!StringUtils.hasText(farmId) || !StringUtils.hasText(period)) {
            log.warn("farmId or period is blank, farmId={}, period={}", farmId, period);
            return;
        }

        try {
            AiEvaluateRequest request = aiFeatureAggregationService.buildAiRequest(farmId, period);

            if (request == null || request.getAggregatedFeatures() == null) {
                log.warn("No valid AI request generated, farmId={}, period={}", farmId, period);
                return;
            }

            AiEvaluateResponse response = farm2FutureAiClient.evaluate(request);

            if (response == null) {
                log.warn("AI response is null, farmId={}, period={}", farmId, period);
                return;
            }

            if (!StringUtils.hasText(response.getFarmId())) {
                response.setFarmId(farmId);
            }

            if (!StringUtils.hasText(response.getPeriod())) {
                response.setPeriod(period);
            }

            saveOrUpdateEsgScore(response);

            log.info(
                    "ESG AI result saved, farmId={}, period={}, grade={}, score={}",
                    response.getFarmId(),
                    response.getPeriod(),
                    response.getEsgGrade(),
                    response.getOverallEsgScore()
            );

        } catch (Exception e) {
            log.error("ESG AI evaluation failed, farmId={}, period={}", farmId, period, e);
        }
    }

    private void saveOrUpdateEsgScore(AiEvaluateResponse response) {
        EsgScore existing = esgScoreMapper.selectOne(
                new LambdaQueryWrapper<EsgScore>()
                        .eq(EsgScore::getFarmId, response.getFarmId())
                        .eq(EsgScore::getPeriod, response.getPeriod())
                        .eq(EsgScore::getDeleted, 0)
                        .last("LIMIT 1")
        );

        if (existing == null) {
            EsgScore esgScore = new EsgScore();

            esgScore.setFarmId(response.getFarmId());
            esgScore.setPeriod(response.getPeriod());

            esgScore.setEnvironmentalScore(defaultScore(response.getEnvironmentalScore()));
            esgScore.setSocialScore(defaultScore(response.getSocialScore()));
            esgScore.setGovernanceScore(defaultScore(response.getGovernanceScore()));
            esgScore.setOverallScore(defaultScore(response.getOverallEsgScore()));

            esgScore.setGrade(response.getEsgGrade());
            esgScore.setSuggestion(response.getXaiExplanation());
            esgScore.setSuggestedTokenAmount(response.getSuggestedTokenAmount());

            esgScore.setAnomalyDetected(Boolean.TRUE.equals(response.getAnomalyDetected()) ? 1 : 0);
            esgScore.setEsgCompliant(Boolean.TRUE.equals(response.getEsgCompliant()) ? 1 : 0);

            esgScore.setDeleted(0);
            esgScore.setCreateTime(LocalDateTime.now());
            esgScore.setUpdateTime(LocalDateTime.now());

            esgScoreMapper.insert(esgScore);
            return;
        }

        existing.setEnvironmentalScore(defaultScore(response.getEnvironmentalScore()));
        existing.setSocialScore(defaultScore(response.getSocialScore()));
        existing.setGovernanceScore(defaultScore(response.getGovernanceScore()));
        existing.setOverallScore(defaultScore(response.getOverallEsgScore()));

        existing.setGrade(response.getEsgGrade());
        existing.setSuggestion(response.getXaiExplanation());
        existing.setSuggestedTokenAmount(response.getSuggestedTokenAmount());

        existing.setAnomalyDetected(Boolean.TRUE.equals(response.getAnomalyDetected()) ? 1 : 0);
        existing.setEsgCompliant(Boolean.TRUE.equals(response.getEsgCompliant()) ? 1 : 0);

        existing.setUpdateTime(LocalDateTime.now());

        esgScoreMapper.updateById(existing);
    }

    private java.math.BigDecimal defaultScore(java.math.BigDecimal value) {
        return value == null ? java.math.BigDecimal.ZERO : value;
    }

    private static String toYearMonth(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }
}