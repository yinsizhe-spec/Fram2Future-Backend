package com.farm2future.farm2future_backend.model.report.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.farm2future.farm2future_backend.model.fram.entity.Farm;
import com.farm2future.farm2future_backend.model.fram.entity.FarmBatch;
import com.farm2future.farm2future_backend.model.fram.entity.IotSnapshot;
import com.farm2future.farm2future_backend.model.fram.mapper.FarmBatchMapper;
import com.farm2future.farm2future_backend.model.fram.mapper.FarmMapper;
import com.farm2future.farm2future_backend.model.fram.mapper.IotSnapshotMapper;
import com.farm2future.farm2future_backend.model.report.dto.AiEvaluateRequest;
import com.farm2future.farm2future_backend.model.report.dto.AiEvaluateResponse;
import com.farm2future.farm2future_backend.model.report.dto.EsgReportGenerateRequest;
import com.farm2future.farm2future_backend.model.report.dto.EsgReportGenerateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EsgReportService {

    private final RestTemplate restTemplate;
    private final FarmMapper farmMapper;
    private final FarmBatchMapper farmBatchMapper;
    private final IotSnapshotMapper iotSnapshotMapper;

    @Value("${farm2future.ai.evaluate-url:https://farm2futureai.vercel.app/api/v1/ai/evaluate}")
    private String aiEvaluateUrl;

    public EsgReportGenerateResponse generate(EsgReportGenerateRequest request) {
        AiEvaluateRequest aiRequest = buildAiRequestFromDatabase(request);
        AiEvaluateResponse aiResponse = callAiEvaluateApi(aiRequest);
        return buildFrontendResponse(request, aiRequest, aiResponse);
    }

    private AiEvaluateRequest buildAiRequestFromDatabase(EsgReportGenerateRequest request) {
        LocalDate fromDate = LocalDate.parse(request.getFrom());
        LocalDate toDate = LocalDate.parse(request.getTo());

        List<Farm> farms = queryTargetFarms(request.getEntity());

        if (farms.isEmpty()) {
            return buildEmptyAiRequest(request);
        }

        List<String> farmIds = farms.stream()
                .map(Farm::getId)
                .collect(Collectors.toList());

        List<FarmBatch> batches = farmBatchMapper.selectList(
                new LambdaQueryWrapper<FarmBatch>()
                        .in(FarmBatch::getFarmId, farmIds)
                        .ge(FarmBatch::getBatchDate, fromDate)
                        .le(FarmBatch::getBatchDate, toDate)
                        .eq(FarmBatch::getDeleted, 0)
        );

        if (batches.isEmpty()) {
            return buildEmptyAiRequest(request);
        }

        List<String> batchIds = batches.stream()
                .map(FarmBatch::getId)
                .collect(Collectors.toList());

        List<IotSnapshot> snapshots = iotSnapshotMapper.selectList(
                new LambdaQueryWrapper<IotSnapshot>()
                        .in(IotSnapshot::getFarmId, farmIds)
                        .in(IotSnapshot::getBatchId, batchIds)
                        .eq(IotSnapshot::getDeleted, 0)
        );

        Map<String, BigDecimal> aggregatedFeatures = calculateAggregatedFeatures(batches, snapshots);

        AiEvaluateRequest aiRequest = new AiEvaluateRequest();

        if ("All Entities".equalsIgnoreCase(request.getEntity())) {
            aiRequest.setFarmId("All Entities");
        } else {
            aiRequest.setFarmId(farms.get(0).getId());
        }

        aiRequest.setPeriod(buildPeriodText(request));
        aiRequest.setAggregatedFeatures(aggregatedFeatures);

        return aiRequest;
    }

    private AiEvaluateRequest buildEmptyAiRequest(EsgReportGenerateRequest request) {
        AiEvaluateRequest aiRequest = new AiEvaluateRequest();

        if ("All Entities".equalsIgnoreCase(request.getEntity())) {
            aiRequest.setFarmId("All Entities");
        } else {
            aiRequest.setFarmId(request.getEntity());
        }

        aiRequest.setPeriod(buildPeriodText(request));

        Map<String, BigDecimal> features = new LinkedHashMap<>();

        features.put("resource_efficiency", bd("0.50"));
        features.put("chemical_compliance", bd("0.50"));
        features.put("labor_equity_score", bd("0.50"));
        features.put("supply_chain_trans", bd("0.50"));
        features.put("compliance_stability", bd("0.50"));
        features.put("system_integrity", bd("0.50"));

        aiRequest.setAggregatedFeatures(features);

        return aiRequest;
    }

    /**
     * 根据前端传来的开始日期 from 自动计算季度。
     *
     * 例如：
     * 2026-01-01 -> 2026-Q1
     * 2026-04-01 -> 2026-Q2
     * 2026-07-01 -> 2026-Q3
     * 2026-10-01 -> 2026-Q4
     */
    private String buildPeriodText(EsgReportGenerateRequest request) {
        LocalDate fromDate = LocalDate.parse(request.getFrom());

        int month = fromDate.getMonthValue();
        int quarter = ((month - 1) / 3) + 1;

        return fromDate.getYear() + "-Q" + quarter;
    }

    private List<Farm> queryTargetFarms(String entity) {
        LambdaQueryWrapper<Farm> wrapper = new LambdaQueryWrapper<Farm>()
                .eq(Farm::getDeleted, 0);

        if (!"All Entities".equalsIgnoreCase(entity)) {
            wrapper.and(w -> w
                    .eq(Farm::getId, entity)
                    .or()
                    .eq(Farm::getFarmName, entity)
            );
        }

        return farmMapper.selectList(wrapper);
    }

    private AiEvaluateResponse callAiEvaluateApi(AiEvaluateRequest aiRequest) {
        try {
            ResponseEntity<AiEvaluateResponse> responseEntity = restTemplate.postForEntity(
                    aiEvaluateUrl,
                    aiRequest,
                    AiEvaluateResponse.class
            );

            return responseEntity.getBody();
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, BigDecimal> calculateAggregatedFeatures(
            List<FarmBatch> batches,
            List<IotSnapshot> snapshots
    ) {
        Map<String, BigDecimal> features = new LinkedHashMap<>();

        features.put("resource_efficiency", calculateResourceEfficiency(batches));
        features.put("chemical_compliance", calculateChemicalCompliance(batches));
        features.put("labor_equity_score", calculateLaborEquityScore(batches));
        features.put("supply_chain_trans", calculateSupplyChainTransparency(batches));
        features.put("compliance_stability", calculateComplianceStability(snapshots));
        features.put("system_integrity", calculateSystemIntegrity(batches, snapshots));

        return features;
    }

    private BigDecimal calculateResourceEfficiency(List<FarmBatch> batches) {
        BigDecimal totalYield = sum(
                batches.stream()
                        .map(FarmBatch::getYieldKg)
                        .collect(Collectors.toList())
        );

        BigDecimal totalWater = sum(
                batches.stream()
                        .map(FarmBatch::getWaterUsageL)
                        .collect(Collectors.toList())
        );

        if (isZero(totalYield) || isZero(totalWater)) {
            return bd("0.50");
        }

        BigDecimal kgPerLiter = totalYield.divide(totalWater, 6, RoundingMode.HALF_UP);

        return normalize(kgPerLiter, bd("0.00"), bd("0.50"));
    }

    private BigDecimal calculateChemicalCompliance(List<FarmBatch> batches) {
        BigDecimal totalFertiliser = sum(
                batches.stream()
                        .map(FarmBatch::getFertiliserUsageKg)
                        .collect(Collectors.toList())
        );

        BigDecimal totalYield = sum(
                batches.stream()
                        .map(FarmBatch::getYieldKg)
                        .collect(Collectors.toList())
        );

        if (isZero(totalYield)) {
            return bd("0.50");
        }

        BigDecimal fertiliserIntensity = totalFertiliser.divide(totalYield, 6, RoundingMode.HALF_UP);

        return normalizeReverse(fertiliserIntensity, bd("0.05"), bd("0.30"));
    }

    private BigDecimal calculateLaborEquityScore(List<FarmBatch> batches) {
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        for (FarmBatch batch : batches) {
            BigDecimal saleQuantity = nvl(batch.getSaleQuantityKg());
            BigDecimal saleUnitPrice = nvl(batch.getSaleUnitPriceRm());
            BigDecimal seedCost = nvl(batch.getSeedCostRm());
            BigDecimal fertiliserCost = nvl(batch.getFertiliserCostRm());

            totalRevenue = totalRevenue.add(saleQuantity.multiply(saleUnitPrice));
            totalCost = totalCost.add(seedCost).add(fertiliserCost);
        }

        if (isZero(totalRevenue)) {
            return bd("0.50");
        }

        BigDecimal profitRate = totalRevenue.subtract(totalCost)
                .divide(totalRevenue, 6, RoundingMode.HALF_UP);

        return normalize(profitRate, bd("0.00"), bd("0.50"));
    }

    private BigDecimal calculateSupplyChainTransparency(List<FarmBatch> batches) {
        if (batches.isEmpty()) {
            return bd("0.50");
        }

        int completeCount = 0;

        for (FarmBatch batch : batches) {
            boolean hasBuyer = batch.getBuyerName() != null && !batch.getBuyerName().isBlank();
            boolean hasTxHash = batch.getTxHash() != null && !batch.getTxHash().isBlank();

            if (hasBuyer && hasTxHash) {
                completeCount++;
            }
        }

        return BigDecimal.valueOf(completeCount)
                .divide(BigDecimal.valueOf(batches.size()), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateComplianceStability(List<IotSnapshot> snapshots) {
        if (snapshots.isEmpty()) {
            return bd("0.60");
        }

        int validCount = 0;

        for (IotSnapshot snapshot : snapshots) {
            boolean soilMoistureOk = between(snapshot.getSoilMoisturePct(), bd("20"), bd("90"));
            boolean temperatureOk = between(snapshot.getTemperatureC(), bd("15"), bd("40"));
            boolean humidityOk = between(snapshot.getHumidityPct(), bd("30"), bd("95"));
            boolean phOk = between(snapshot.getPhLevel(), bd("5.5"), bd("7.5"));

            if (soilMoistureOk && temperatureOk && humidityOk && phOk) {
                validCount++;
            }
        }

        return BigDecimal.valueOf(validCount)
                .divide(BigDecimal.valueOf(snapshots.size()), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateSystemIntegrity(
            List<FarmBatch> batches,
            List<IotSnapshot> snapshots
    ) {
        if (batches.isEmpty()) {
            return bd("0.00");
        }

        Set<String> snapshotBatchIds = snapshots.stream()
                .map(IotSnapshot::getBatchId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        BigDecimal totalScore = BigDecimal.ZERO;

        for (FarmBatch batch : batches) {
            int filled = 0;
            int total = 10;

            if (batch.getYieldKg() != null) filled++;
            if (batch.getWaterUsageL() != null) filled++;
            if (batch.getFertiliserUsageKg() != null) filled++;
            if (batch.getSaleQuantityKg() != null) filled++;
            if (batch.getSaleUnitPriceRm() != null) filled++;
            if (batch.getSeedCostRm() != null) filled++;
            if (batch.getFertiliserCostRm() != null) filled++;
            if (batch.getBuyerName() != null && !batch.getBuyerName().isBlank()) filled++;
            if (batch.getTxHash() != null && !batch.getTxHash().isBlank()) filled++;
            if (snapshotBatchIds.contains(batch.getId())) filled++;

            BigDecimal batchScore = BigDecimal.valueOf(filled)
                    .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP);

            totalScore = totalScore.add(batchScore);
        }

        return totalScore.divide(BigDecimal.valueOf(batches.size()), 4, RoundingMode.HALF_UP);
    }

    private EsgReportGenerateResponse buildFrontendResponse(
            EsgReportGenerateRequest request,
            AiEvaluateRequest aiRequest,
            AiEvaluateResponse aiResponse
    ) {
        EsgReportGenerateResponse response = new EsgReportGenerateResponse();

        response.setPeriod(new EsgReportGenerateResponse.Period(
                request.getFrom(),
                request.getTo()
        ));

        response.setEntity(request.getEntity());
        response.setScores(buildScores(aiRequest, aiResponse));
        response.setRiskFlags(buildRiskFlags(aiResponse, response.getScores()));
        response.setGeneratedAt(Instant.now().toString());

        return response;
    }

    private List<EsgReportGenerateResponse.ScoreItem> buildScores(
            AiEvaluateRequest aiRequest,
            AiEvaluateResponse aiResponse
    ) {
        int environmentalScore = averageFeatureScore(
                aiRequest,
                "resource_efficiency",
                "chemical_compliance"
        );

        int socialScore = averageFeatureScore(
                aiRequest,
                "labor_equity_score"
        );

        int governanceScore = averageFeatureScore(
                aiRequest,
                "supply_chain_trans",
                "compliance_stability",
                "system_integrity"
        );

        String explanation = "ESG score is calculated from backend aggregated farm indicators.";
        String grade = "N/A";
        Integer finalScore = null;

        if (aiResponse != null) {
            if (hasText(aiResponse.getXaiExplanation())) {
                explanation = aiResponse.getXaiExplanation();
            }

            if (hasText(aiResponse.getEsgGrade())) {
                grade = aiResponse.getEsgGrade();
            }

            if (aiResponse.getFinalEsgScore() != null) {
                finalScore = roundScore(aiResponse.getFinalEsgScore());
            }
        }

        String environmentalNote;

        if (finalScore == null) {
            environmentalNote = explanation;
        } else {
            environmentalNote = "AI final ESG score: " + finalScore + ", grade: " + grade + ". " + explanation;
        }

        List<EsgReportGenerateResponse.ScoreItem> scores = new ArrayList<>();

        scores.add(new EsgReportGenerateResponse.ScoreItem(
                "Environmental (E)",
                environmentalScore,
                environmentalNote
        ));

        scores.add(new EsgReportGenerateResponse.ScoreItem(
                "Social (S)",
                socialScore,
                "Calculated from labor equity, income sustainability and cost indicators."
        ));

        scores.add(new EsgReportGenerateResponse.ScoreItem(
                "Governance (G)",
                governanceScore,
                "Calculated from supply chain transparency, compliance stability and system integrity."
        ));

        return scores;
    }

    private List<EsgReportGenerateResponse.RiskFlag> buildRiskFlags(
            AiEvaluateResponse aiResponse,
            List<EsgReportGenerateResponse.ScoreItem> scores
    ) {
        List<EsgReportGenerateResponse.RiskFlag> flags = new ArrayList<>();

        int environmental = findScore(scores, "Environmental (E)");
        int social = findScore(scores, "Social (S)");
        int governance = findScore(scores, "Governance (G)");

        boolean isDefaultNoDataScore = environmental == 50 && social == 50 && governance == 50;

        if (isDefaultNoDataScore && aiResponse == null) {
            flags.add(new EsgReportGenerateResponse.RiskFlag(
                    "warning",
                    "No ESG Data Available",
                    "No farm batch or IoT data was found for the selected period, or the AI service did not return a response. The report is generated using default neutral values."
            ));

            return flags;
        }

        if (aiResponse == null) {
            flags.add(new EsgReportGenerateResponse.RiskFlag(
                    "warning",
                    "AI Service Unavailable",
                    "The report was generated using backend-calculated ESG indicators because the AI evaluation service did not return a response."
            ));

            addLocalScoreRiskFlags(flags, environmental, social, governance);

            return flags;
        }

        if (Boolean.TRUE.equals(aiResponse.getAnomalyDetected())) {
            flags.add(new EsgReportGenerateResponse.RiskFlag(
                    "danger",
                    "Anomaly Detected",
                    hasText(aiResponse.getXaiExplanation())
                            ? aiResponse.getXaiExplanation()
                            : "The AI service detected abnormal ESG indicators in the selected reporting period."
            ));
        }

        if (Boolean.TRUE.equals(aiResponse.getEsgCompliant())) {
            flags.add(new EsgReportGenerateResponse.RiskFlag(
                    "success",
                    "ESG Compliance Passed",
                    "The AI service marked this report as ESG compliant."
            ));
        } else if (Boolean.FALSE.equals(aiResponse.getEsgCompliant())) {
            flags.add(new EsgReportGenerateResponse.RiskFlag(
                    "warning",
                    "ESG Compliance Review Required",
                    "The AI service marked this report as not fully ESG compliant. Please review environmental, social and governance indicators."
            ));
        }

        if (aiResponse.getFinalEsgScore() != null) {
            int score = roundScore(aiResponse.getFinalEsgScore());

            if (score >= 80) {
                flags.add(new EsgReportGenerateResponse.RiskFlag(
                        "success",
                        "High ESG Performance",
                        "Final ESG score is " + score + " with grade " + safeText(aiResponse.getEsgGrade()) + "."
                ));
            } else if (score >= 60) {
                flags.add(new EsgReportGenerateResponse.RiskFlag(
                        "warning",
                        "Moderate ESG Performance",
                        "Final ESG score is " + score + " with grade " + safeText(aiResponse.getEsgGrade()) + ". Improvement is recommended."
                ));
            } else {
                flags.add(new EsgReportGenerateResponse.RiskFlag(
                        "danger",
                        "Low ESG Performance",
                        "Final ESG score is " + score + " with grade " + safeText(aiResponse.getEsgGrade()) + ". Immediate improvement is required."
                ));
            }
        }

        if (aiResponse.getSuggestedTokenAmount() != null) {
            flags.add(new EsgReportGenerateResponse.RiskFlag(
                    "success",
                    "Suggested Token Reward",
                    "Suggested token amount: " + aiResponse.getSuggestedTokenAmount()
                            .stripTrailingZeros()
                            .toPlainString()
            ));
        }

        if (flags.isEmpty()) {
            addLocalScoreRiskFlags(flags, environmental, social, governance);
        }

        return flags;
    }

    private void addLocalScoreRiskFlags(
            List<EsgReportGenerateResponse.RiskFlag> flags,
            int environmental,
            int social,
            int governance
    ) {
        if (environmental >= 80) {
            flags.add(new EsgReportGenerateResponse.RiskFlag(
                    "success",
                    "Water Usage Optimization",
                    "Environmental performance is stable. Resource efficiency and chemical compliance are in a healthy range."
            ));
        } else if (environmental >= 60) {
            flags.add(new EsgReportGenerateResponse.RiskFlag(
                    "warning",
                    "Environmental Improvement Needed",
                    "Environmental indicators are acceptable, but water usage or fertilizer usage should be reviewed."
            ));
        } else {
            flags.add(new EsgReportGenerateResponse.RiskFlag(
                    "danger",
                    "High Environmental Risk",
                    "Environmental score is low. Water usage and fertilizer usage require immediate attention."
            ));
        }

        if (social >= 80) {
            flags.add(new EsgReportGenerateResponse.RiskFlag(
                    "success",
                    "Fair Income Performance",
                    "Revenue and cost indicators show a healthy income pattern for the selected period."
            ));
        } else if (social >= 60) {
            flags.add(new EsgReportGenerateResponse.RiskFlag(
                    "warning",
                    "Social Performance Review",
                    "Social score is moderate. Please review income, cost and farmer benefit indicators."
            ));
        } else {
            flags.add(new EsgReportGenerateResponse.RiskFlag(
                    "danger",
                    "Low Social Score",
                    "Social indicators are weak. Income sustainability or labor equity may need improvement."
            ));
        }

        if (governance >= 80) {
            flags.add(new EsgReportGenerateResponse.RiskFlag(
                    "success",
                    "Compliance Audits Passed",
                    "Supply chain traceability, IoT records and system integrity are stable."
            ));
        } else if (governance >= 60) {
            flags.add(new EsgReportGenerateResponse.RiskFlag(
                    "warning",
                    "Governance Data Gap",
                    "Some governance data may be incomplete. Please check buyer, transaction hash and IoT records."
            ));
        } else {
            flags.add(new EsgReportGenerateResponse.RiskFlag(
                    "danger",
                    "Governance Risk",
                    "Governance score is low. Traceability or compliance records require immediate review."
            ));
        }
    }

    private int averageFeatureScore(
            AiEvaluateRequest aiRequest,
            String... featureKeys
    ) {
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;

        for (String featureKey : featureKeys) {
            BigDecimal value = aiRequest.getAggregatedFeatures().get(featureKey);

            if (value != null) {
                total = total.add(value);
                count++;
            }
        }

        if (count == 0) {
            return 0;
        }

        BigDecimal average = total.divide(BigDecimal.valueOf(count), 4, RoundingMode.HALF_UP);

        return roundScore(average.multiply(BigDecimal.valueOf(100)));
    }

    private int findScore(List<EsgReportGenerateResponse.ScoreItem> scores, String label) {
        for (EsgReportGenerateResponse.ScoreItem score : scores) {
            if (label.equals(score.getLabel())) {
                return score.getScore();
            }
        }

        return 0;
    }

    private BigDecimal normalize(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value == null) {
            return bd("0.50");
        }

        if (value.compareTo(min) <= 0) {
            return BigDecimal.ZERO;
        }

        if (value.compareTo(max) >= 0) {
            return BigDecimal.ONE;
        }

        return value.subtract(min)
                .divide(max.subtract(min), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizeReverse(BigDecimal value, BigDecimal good, BigDecimal bad) {
        if (value == null) {
            return bd("0.50");
        }

        if (value.compareTo(good) <= 0) {
            return BigDecimal.ONE;
        }

        if (value.compareTo(bad) >= 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.ONE.subtract(
                value.subtract(good)
                        .divide(bad.subtract(good), 4, RoundingMode.HALF_UP)
        );
    }

    private boolean between(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value == null) {
            return false;
        }

        return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    }

    private BigDecimal sum(List<BigDecimal> values) {
        BigDecimal total = BigDecimal.ZERO;

        for (BigDecimal value : values) {
            if (value != null) {
                total = total.add(value);
            }
        }

        return total;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private boolean isZero(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) == 0;
    }

    private BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private int roundScore(BigDecimal value) {
        if (value == null) {
            return 0;
        }

        return clampScore(
                value.setScale(0, RoundingMode.HALF_UP).intValue()
        );
    }

    private int clampScore(int score) {
        if (score < 0) {
            return 0;
        }

        if (score > 100) {
            return 100;
        }

        return score;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String safeText(String value) {
        if (value == null || value.isBlank()) {
            return "N/A";
        }

        return value;
    }
}