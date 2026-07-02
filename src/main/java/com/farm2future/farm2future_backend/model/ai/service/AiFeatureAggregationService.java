package com.farm2future.farm2future_backend.model.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.farm2future.farm2future_backend.model.ai.dto.AiEvaluateRequest;
import com.farm2future.farm2future_backend.model.fram.entity.FarmBatch;
import com.farm2future.farm2future_backend.model.fram.entity.IotSnapshot;
import com.farm2future.farm2future_backend.model.fram.mapper.FarmBatchMapper;
import com.farm2future.farm2future_backend.model.fram.mapper.IotSnapshotMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiFeatureAggregationService {

    private final FarmBatchMapper farmBatchMapper;
    private final IotSnapshotMapper iotSnapshotMapper;

    public AiEvaluateRequest buildAiRequest(String farmId, String period) {
        if (!StringUtils.hasText(farmId) || !StringUtils.hasText(period)) {
            return null;
        }

        List<FarmBatch> batches = farmBatchMapper.selectList(
                        new LambdaQueryWrapper<FarmBatch>()
                                .eq(FarmBatch::getFarmId, farmId)
                                .eq(FarmBatch::getDeleted, 0)
                )
                .stream()
                .filter(batch -> period.equals(toYearMonth(batch.getBatchDate())))
                .toList();

        if (batches.isEmpty()) {
            return null;
        }

        List<String> batchIds = batches.stream()
                .map(FarmBatch::getId)
                .filter(StringUtils::hasText)
                .toList();

        List<IotSnapshot> snapshots = batchIds.isEmpty()
                ? List.of()
                : iotSnapshotMapper.selectList(
                new LambdaQueryWrapper<IotSnapshot>()
                        .in(IotSnapshot::getBatchId, batchIds)
                        .eq(IotSnapshot::getDeleted, 0)
        );

        BigDecimal totalYieldKg = sumYieldKg(batches);
        BigDecimal totalWaterUsageL = sumWaterUsageL(batches);
        BigDecimal totalFertiliserUsageKg = sumFertiliserUsageKg(batches);
        BigDecimal totalSaleQuantityKg = sumSaleQuantityKg(batches);

        AiEvaluateRequest request = new AiEvaluateRequest();
        request.setFarmId(farmId);
        request.setPeriod(period);

        AiEvaluateRequest.AggregatedFeatures features = new AiEvaluateRequest.AggregatedFeatures();

        features.setResourceEfficiency(
                calculateResourceEfficiency(totalYieldKg, totalWaterUsageL)
        );

        features.setChemicalCompliance(
                calculateChemicalCompliance(totalFertiliserUsageKg, totalYieldKg)
        );

        features.setLaborEquityScore(
                calculateLaborEquityScore(batches)
        );

        features.setSupplyChainTrans(
                calculateSupplyChainTransparency(
                        batches,
                        totalSaleQuantityKg,
                        totalYieldKg
                )
        );

        features.setComplianceStability(
                calculateComplianceStability(snapshots)
        );

        features.setSystemIntegrity(
                calculateSystemIntegrity(batches, snapshots)
        );

        request.setAggregatedFeatures(features);
        return request;
    }

    private String toYearMonth(LocalDate date) {
        if (date == null) {
            return null;
        }

        return date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    /**
     * resource_efficiency:
     * 用产量 / 用水量计算。
     */
    private BigDecimal calculateResourceEfficiency(BigDecimal yieldKg, BigDecimal waterUsageL) {
        if (isZero(yieldKg) || isZero(waterUsageL)) {
            return BigDecimal.ZERO;
        }

        BigDecimal value = yieldKg.divide(waterUsageL, 6, RoundingMode.HALF_UP);

        return clamp(value);
    }

    /**
     * chemical_compliance:
     * 你的数据库目前有 fertiliser_usage_kg，没有 pesticide_usage_kg。
     * 所以这里使用肥料用量 / 产量估算化学投入合规程度。
     */
    private BigDecimal calculateChemicalCompliance(BigDecimal fertiliserUsageKg, BigDecimal yieldKg) {
        if (isZero(yieldKg)) {
            return BigDecimal.ZERO;
        }

        BigDecimal fertiliserPerYield = safe(fertiliserUsageKg)
                .divide(yieldKg, 6, RoundingMode.HALF_UP);

        BigDecimal score = BigDecimal.ONE.subtract(fertiliserPerYield);

        return clamp(score);
    }

    /**
     * labor_equity_score:
     * 当前数据库没有 labor_hours / fair_wage_flag。
     * 不能写死分数，所以这里用成本数据完整度估算。
     * 后面如果加了劳工字段，这里再替换成真实劳工公平性计算。
     */
    private BigDecimal calculateLaborEquityScore(List<FarmBatch> batches) {
        if (batches == null || batches.isEmpty()) {
            return BigDecimal.ZERO;
        }

        long validCount = batches.stream()
                .filter(batch -> batch.getSeedCostRm() != null)
                .filter(batch -> batch.getFertiliserCostRm() != null)
                .count();

        return ratio(
                BigDecimal.valueOf(validCount),
                BigDecimal.valueOf(batches.size())
        );
    }

    /**
     * supply_chain_trans:
     * 用销售数据完整度 + 销售数量合理性估算供应链透明度。
     */
    private BigDecimal calculateSupplyChainTransparency(
            List<FarmBatch> batches,
            BigDecimal totalSaleQuantityKg,
            BigDecimal totalYieldKg
    ) {
        if (batches == null || batches.isEmpty()) {
            return BigDecimal.ZERO;
        }

        long completeSaleCount = batches.stream()
                .filter(batch -> StringUtils.hasText(batch.getBuyerName()))
                .filter(batch -> batch.getSaleQuantityKg() != null)
                .filter(batch -> batch.getSaleUnitPriceRm() != null)
                .count();

        BigDecimal completenessScore = ratio(
                BigDecimal.valueOf(completeSaleCount),
                BigDecimal.valueOf(batches.size())
        );

        if (isZero(totalYieldKg)) {
            return completenessScore;
        }

        BigDecimal saleRatio = safe(totalSaleQuantityKg)
                .divide(totalYieldKg, 4, RoundingMode.HALF_UP);

        BigDecimal saleReasonableScore = clamp(saleRatio);

        return clamp(
                completenessScore.add(saleReasonableScore)
                        .divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP)
        );
    }

    /**
     * compliance_stability:
     * 用 IoT 数据是否在合理范围内估算合规稳定性。
     */
    private BigDecimal calculateComplianceStability(List<IotSnapshot> snapshots) {
        if (snapshots == null || snapshots.isEmpty()) {
            return BigDecimal.ZERO;
        }

        long validCount = snapshots.stream()
                .filter(this::isIotSnapshotInReasonableRange)
                .count();

        return ratio(
                BigDecimal.valueOf(validCount),
                BigDecimal.valueOf(snapshots.size())
        );
    }

    private boolean isIotSnapshotInReasonableRange(IotSnapshot snapshot) {
        if (snapshot == null) {
            return false;
        }

        return between(snapshot.getSoilMoisturePct(), BigDecimal.ZERO, new BigDecimal("100"))
                && between(snapshot.getTemperatureC(), new BigDecimal("-10"), new BigDecimal("60"))
                && between(snapshot.getHumidityPct(), BigDecimal.ZERO, new BigDecimal("100"))
                && between(snapshot.getPhLevel(), BigDecimal.ZERO, new BigDecimal("14"));
    }

    /**
     * system_integrity:
     * 用 farm_batch 和 iot_snapshot 的关键字段完整度估算系统完整性。
     */
    private BigDecimal calculateSystemIntegrity(List<FarmBatch> batches, List<IotSnapshot> snapshots) {
        if (batches == null || batches.isEmpty()) {
            return BigDecimal.ZERO;
        }

        long completeBatchCount = batches.stream()
                .filter(batch -> StringUtils.hasText(batch.getId()))
                .filter(batch -> StringUtils.hasText(batch.getFarmId()))
                .filter(batch -> batch.getBatchDate() != null)
                .filter(batch -> batch.getYieldKg() != null)
                .filter(batch -> batch.getWaterUsageL() != null)
                .filter(batch -> batch.getFertiliserUsageKg() != null)
                .filter(batch -> batch.getSubmittedAt() != null || batch.getCreateTime() != null)
                .count();

        BigDecimal batchIntegrity = ratio(
                BigDecimal.valueOf(completeBatchCount),
                BigDecimal.valueOf(batches.size())
        );

        if (snapshots == null || snapshots.isEmpty()) {
            return batchIntegrity;
        }

        long completeSnapshotCount = snapshots.stream()
                .filter(snapshot -> StringUtils.hasText(snapshot.getBatchId()))
                .filter(snapshot -> StringUtils.hasText(snapshot.getFarmId()))
                .filter(snapshot -> snapshot.getSoilMoisturePct() != null)
                .filter(snapshot -> snapshot.getTemperatureC() != null)
                .filter(snapshot -> snapshot.getHumidityPct() != null)
                .filter(snapshot -> snapshot.getPhLevel() != null)
                .filter(snapshot -> snapshot.getCreateTime() != null)
                .count();

        BigDecimal snapshotIntegrity = ratio(
                BigDecimal.valueOf(completeSnapshotCount),
                BigDecimal.valueOf(snapshots.size())
        );

        return clamp(
                batchIntegrity.add(snapshotIntegrity)
                        .divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP)
        );
    }

    private boolean between(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value == null) {
            return false;
        }

        return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    }

    private BigDecimal ratio(BigDecimal numerator, BigDecimal denominator) {
        if (isZero(denominator)) {
            return BigDecimal.ZERO;
        }

        return clamp(numerator.divide(denominator, 4, RoundingMode.HALF_UP));
    }

    private BigDecimal clamp(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        if (value.compareTo(BigDecimal.ONE) > 0) {
            return BigDecimal.ONE;
        }

        return value.setScale(4, RoundingMode.HALF_UP);
    }

    private boolean isZero(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) == 0;
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal sumYieldKg(List<FarmBatch> batches) {
        return batches.stream()
                .map(FarmBatch::getYieldKg)
                .map(this::safe)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumWaterUsageL(List<FarmBatch> batches) {
        return batches.stream()
                .map(FarmBatch::getWaterUsageL)
                .map(this::safe)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumFertiliserUsageKg(List<FarmBatch> batches) {
        return batches.stream()
                .map(FarmBatch::getFertiliserUsageKg)
                .map(this::safe)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumSaleQuantityKg(List<FarmBatch> batches) {
        return batches.stream()
                .map(FarmBatch::getSaleQuantityKg)
                .map(this::safe)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}