package com.farm2future.farm2future_backend.model.fram.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.farm2future.farm2future_backend.model.fram.entity.DashboardAlert;
import com.farm2future.farm2future_backend.model.fram.entity.DashboardChartPoint;
import com.farm2future.farm2future_backend.model.fram.entity.EsgScore;
import com.farm2future.farm2future_backend.model.fram.entity.Farm;
import com.farm2future.farm2future_backend.model.fram.entity.FarmBatch;
import com.farm2future.farm2future_backend.model.fram.mapper.DashboardAlertMapper;
import com.farm2future.farm2future_backend.model.fram.mapper.DashboardChartPointMapper;
import com.farm2future.farm2future_backend.model.fram.mapper.EsgScoreMapper;
import com.farm2future.farm2future_backend.model.fram.mapper.FarmBatchMapper;
import com.farm2future.farm2future_backend.model.fram.mapper.FarmMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EsgDailyStatisticsService {

    private final FarmMapper farmMapper;
    private final FarmBatchMapper farmBatchMapper;
    private final EsgScoreMapper esgScoreMapper;
    private final DashboardChartPointMapper dashboardChartPointMapper;
    private final DashboardAlertMapper dashboardAlertMapper;

    /**
     * 统计某个月所有农场 ESG 数据
     */
    @Transactional
    public void calculateMonthlyEsgScore(YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Farm> farms = farmMapper.selectList(
                new LambdaQueryWrapper<Farm>()
                        .eq(Farm::getDeleted, 0)
        );

        for (Farm farm : farms) {
            calculateSingleFarmMonthlyEsgScore(farm, yearMonth, startDate, endDate);
        }

        updateDashboardChartPoint(yearMonth);
    }

    /**
     * 统计单个农场一个月的 ESG 分数
     */
    private void calculateSingleFarmMonthlyEsgScore(
            Farm farm,
            YearMonth yearMonth,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<FarmBatch> batches = farmBatchMapper.selectList(
                new LambdaQueryWrapper<FarmBatch>()
                        .eq(FarmBatch::getFarmId, farm.getId())
                        .eq(FarmBatch::getDeleted, 0)
                        .between(FarmBatch::getBatchDate, startDate, endDate)
        );

        if (batches == null || batches.isEmpty()) {
            return;
        }

        BigDecimal totalYieldKg = sumYieldKg(batches);
        BigDecimal totalWaterUsageL = sumWaterUsageL(batches);
        BigDecimal totalFertiliserUsageKg = sumFertiliserUsageKg(batches);
        BigDecimal totalSaleQuantityKg = sumSaleQuantityKg(batches);
        BigDecimal totalRevenueRm = calculateRevenueRm(batches);
        BigDecimal totalCostRm = calculateCostRm(batches);

        BigDecimal environmentalScore = calculateEnvironmentalScore(
                totalYieldKg,
                totalWaterUsageL,
                totalFertiliserUsageKg
        );

        BigDecimal socialScore = calculateSocialScore(
                totalYieldKg,
                totalSaleQuantityKg
        );

        BigDecimal governanceScore = calculateGovernanceScore(
                totalRevenueRm,
                totalCostRm
        );

        BigDecimal totalScore = environmentalScore
                .multiply(new BigDecimal("0.40"))
                .add(socialScore.multiply(new BigDecimal("0.30")))
                .add(governanceScore.multiply(new BigDecimal("0.30")))
                .setScale(2, RoundingMode.HALF_UP);

        saveOrUpdateEsgScore(
                farm.getId(),
                yearMonth.toString(),
                environmentalScore,
                socialScore,
                governanceScore,
                totalScore
        );

        createDashboardAlertIfNeeded(farm, totalScore);
    }

    /**
     * Environmental Score
     *
     * 简单算法：
     * 水用得越少，肥料用得越少，分数越高。
     */
    private BigDecimal calculateEnvironmentalScore(
            BigDecimal totalYieldKg,
            BigDecimal totalWaterUsageL,
            BigDecimal totalFertiliserUsageKg
    ) {
        if (totalYieldKg.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal waterPerKg = totalWaterUsageL.divide(totalYieldKg, 4, RoundingMode.HALF_UP);
        BigDecimal fertiliserPerKg = totalFertiliserUsageKg.divide(totalYieldKg, 4, RoundingMode.HALF_UP);

        BigDecimal score = new BigDecimal("100");

        score = score.subtract(waterPerKg.multiply(new BigDecimal("0.50")));
        score = score.subtract(fertiliserPerKg.multiply(new BigDecimal("2.00")));

        return limitScore(score);
    }

    /**
     * Social Score
     *
     * 简单算法：
     * 销售比例越高，说明农产品流通越好，分数越高。
     */
    private BigDecimal calculateSocialScore(
            BigDecimal totalYieldKg,
            BigDecimal totalSaleQuantityKg
    ) {
        if (totalYieldKg.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal saleRate = totalSaleQuantityKg
                .divide(totalYieldKg, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        return limitScore(saleRate);
    }

    /**
     * Governance Score
     *
     * 简单算法：
     * 利润率越高，治理分越高。
     */
    private BigDecimal calculateGovernanceScore(
            BigDecimal totalRevenueRm,
            BigDecimal totalCostRm
    ) {
        if (totalRevenueRm.compareTo(BigDecimal.ZERO) <= 0) {
            return new BigDecimal("60.00");
        }

        BigDecimal profit = totalRevenueRm.subtract(totalCostRm);

        BigDecimal profitRate = profit
                .divide(totalRevenueRm, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        BigDecimal score = new BigDecimal("60").add(profitRate);

        return limitScore(score);
    }

    /**
     * 保存或更新 esg_score
     */
    private void saveOrUpdateEsgScore(
            String farmId,
            String period,
            BigDecimal environmentalScore,
            BigDecimal socialScore,
            BigDecimal governanceScore,
            BigDecimal totalScore
    ) {
        LocalDateTime now = LocalDateTime.now();

        EsgScore existing = esgScoreMapper.selectOne(
                new LambdaQueryWrapper<EsgScore>()
                        .eq(EsgScore::getFarmId, farmId)
                        .eq(EsgScore::getPeriod, period)
                        .eq(EsgScore::getDeleted, 0)
        );

        EsgScore esgScore = existing == null ? new EsgScore() : existing;

        esgScore.setFarmId(farmId);
        esgScore.setPeriod(period);
        esgScore.setEnvironmentalScore(environmentalScore);
        esgScore.setSocialScore(socialScore);
        esgScore.setGovernanceScore(governanceScore);
        esgScore.setTotalScore(totalScore);
        esgScore.setDeleted(0);
        esgScore.setUpdateTime(now);

        if (existing == null) {
            esgScore.setCreateTime(now);
            esgScoreMapper.insert(esgScore);
        } else {
            esgScoreMapper.updateById(esgScore);
        }
    }

    /**
     * 更新 dashboard_chart_point
     *
     * 统计所有农场当前月份 ESG 平均分，写入 All Farms 图表点。
     */
    private void updateDashboardChartPoint(YearMonth yearMonth) {
        List<EsgScore> scores = esgScoreMapper.selectList(
                new LambdaQueryWrapper<EsgScore>()
                        .eq(EsgScore::getPeriod, yearMonth.toString())
                        .eq(EsgScore::getDeleted, 0)
        );

        if (scores == null || scores.isEmpty()) {
            return;
        }

        BigDecimal total = scores.stream()
                .map(EsgScore::getTotalScore)
                .filter(score -> score != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageScore = total.divide(
                BigDecimal.valueOf(scores.size()),
                2,
                RoundingMode.HALF_UP
        );

        String chartMonth = yearMonth.getMonth()
                .getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

        DashboardChartPoint existing = dashboardChartPointMapper.selectOne(
                new LambdaQueryWrapper<DashboardChartPoint>()
                        .eq(DashboardChartPoint::getEntity, "All Farms")
                        .eq(DashboardChartPoint::getChartYear, yearMonth.getYear())
                        .eq(DashboardChartPoint::getChartMonth, chartMonth)
                        .eq(DashboardChartPoint::getDeleted, 0)
        );

        LocalDateTime now = LocalDateTime.now();

        DashboardChartPoint chartPoint = existing == null ? new DashboardChartPoint() : existing;

        chartPoint.setEntity("All Farms");
        chartPoint.setChartYear(yearMonth.getYear());
        chartPoint.setChartMonth(chartMonth);
        chartPoint.setChartValue(averageScore);
        chartPoint.setDeleted(0);
        chartPoint.setUpdateTime(now);

        if (existing == null) {
            chartPoint.setCreateTime(now);
            dashboardChartPointMapper.insert(chartPoint);
        } else {
            dashboardChartPointMapper.updateById(chartPoint);
        }
    }

    /**
     * ESG 分数过低时生成 dashboard_alert
     */
    private void createDashboardAlertIfNeeded(Farm farm, BigDecimal totalScore) {
        if (totalScore.compareTo(new BigDecimal("60")) >= 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        String severity = totalScore.compareTo(new BigDecimal("40")) < 0
                ? "danger"
                : "warning";

        DashboardAlert alert = new DashboardAlert();
        alert.setTitle("Low ESG score detected");
        alert.setEntity(farm.getFarmName());
        alert.setSeverity(severity);
        alert.setAlertTime(now);
        alert.setDeleted(0);
        alert.setCreateTime(now);
        alert.setUpdateTime(now);

        dashboardAlertMapper.insert(alert);
    }

    private BigDecimal sumYieldKg(List<FarmBatch> batches) {
        return batches.stream()
                .map(FarmBatch::getYieldKg)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumWaterUsageL(List<FarmBatch> batches) {
        return batches.stream()
                .map(FarmBatch::getWaterUsageL)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumFertiliserUsageKg(List<FarmBatch> batches) {
        return batches.stream()
                .map(FarmBatch::getFertiliserUsageKg)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumSaleQuantityKg(List<FarmBatch> batches) {
        return batches.stream()
                .map(FarmBatch::getSaleQuantityKg)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateRevenueRm(List<FarmBatch> batches) {
        return batches.stream()
                .map(batch -> {
                    BigDecimal saleQuantityKg = batch.getSaleQuantityKg() == null
                            ? BigDecimal.ZERO
                            : batch.getSaleQuantityKg();

                    BigDecimal saleUnitPriceRm = batch.getSaleUnitPriceRm() == null
                            ? BigDecimal.ZERO
                            : batch.getSaleUnitPriceRm();

                    return saleQuantityKg.multiply(saleUnitPriceRm);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateCostRm(List<FarmBatch> batches) {
        return batches.stream()
                .map(batch -> {
                    BigDecimal seedCostRm = batch.getSeedCostRm() == null
                            ? BigDecimal.ZERO
                            : batch.getSeedCostRm();

                    BigDecimal fertiliserCostRm = batch.getFertiliserCostRm() == null
                            ? BigDecimal.ZERO
                            : batch.getFertiliserCostRm();

                    return seedCostRm.add(fertiliserCostRm);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal limitScore(BigDecimal score) {
        if (score == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        if (score.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        if (score.compareTo(new BigDecimal("100")) > 0) {
            return new BigDecimal("100.00");
        }

        return score.setScale(2, RoundingMode.HALF_UP);
    }
}