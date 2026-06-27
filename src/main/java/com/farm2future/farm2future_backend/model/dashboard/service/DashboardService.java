package com.farm2future.farm2future_backend.model.dashboard.service;

import com.farm2future.farm2future_backend.common.exception.BusinessException;
import com.farm2future.farm2future_backend.model.dashboard.dto.DashboardAlertRow;
import com.farm2future.farm2future_backend.model.dashboard.dto.DashboardChartPointRow;
import com.farm2future.farm2future_backend.model.dashboard.dto.DashboardOverviewResponse;
import com.farm2future.farm2future_backend.model.dashboard.dto.DashboardStatsRow;
import com.farm2future.farm2future_backend.model.dashboard.mapper.DashboardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private static final String ALL_FARMS = "All Farms";

    private final DashboardMapper dashboardMapper;

    public DashboardOverviewResponse getOverview(String farm) {
        String selectedFarm = normalizeFarm(farm);

        List<String> farms = getFarmOptions();

        validateFarm(selectedFarm, farms);

        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);

        String currentPeriod = currentMonth.toString();
        String previousPeriod = previousMonth.toString();

        DashboardStatsRow currentStats = safeStats(
                dashboardMapper.selectStatsByMonth(selectedFarm, currentPeriod)
        );

        DashboardStatsRow previousStats = safeStats(
                dashboardMapper.selectStatsByMonth(selectedFarm, previousPeriod)
        );

        DashboardOverviewResponse.Changes changes = buildChanges(currentStats, previousStats);

        DashboardOverviewResponse.Stats stats = new DashboardOverviewResponse.Stats(
                currentStats.getOverall(),
                currentStats.getEnvironmental(),
                currentStats.getSocial(),
                currentStats.getGovernance(),
                changes
        );

        DashboardOverviewResponse.Chart chart = buildChart(selectedFarm, currentMonth);

        List<DashboardOverviewResponse.Alert> alerts = dashboardMapper.selectRecentAlerts(selectedFarm)
                .stream()
                .map(this::convertAlert)
                .toList();

        String tier = calculateTier(currentStats.getOverall());

        return new DashboardOverviewResponse(
                tier,
                stats,
                chart,
                alerts,
                farms
        );
    }

    private String normalizeFarm(String farm) {
        if (!StringUtils.hasText(farm)) {
            return ALL_FARMS;
        }

        return farm.trim();
    }

    private List<String> getFarmOptions() {
        List<String> farms = new ArrayList<>();
        farms.add(ALL_FARMS);
        farms.addAll(dashboardMapper.selectFarmNames());
        return farms;
    }

    private void validateFarm(String selectedFarm, List<String> farms) {
        if (!farms.contains(selectedFarm)) {
            throw new BusinessException(
                    HttpStatus.NOT_FOUND,
                    "NOT_FOUND",
                    "Farm not found: " + selectedFarm,
                    selectedFarm
            );
        }
    }

    private DashboardOverviewResponse.Changes buildChanges(
            DashboardStatsRow currentStats,
            DashboardStatsRow previousStats
    ) {
        BigDecimal overallChange = calculatePercentChange(
                currentStats.getOverall(),
                previousStats.getOverall()
        );

        BigDecimal environmentalChange = calculatePercentChange(
                currentStats.getEnvironmental(),
                previousStats.getEnvironmental()
        );

        BigDecimal socialChange = calculatePercentChange(
                currentStats.getSocial(),
                previousStats.getSocial()
        );

        BigDecimal governanceChange = calculatePercentChange(
                currentStats.getGovernance(),
                previousStats.getGovernance()
        );

        return new DashboardOverviewResponse.Changes(
                overallChange,
                environmentalChange,
                socialChange,
                governanceChange
        );
    }

    private BigDecimal calculatePercentChange(BigDecimal current, BigDecimal previous) {
        current = nvl(current);
        previous = nvl(previous);

        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP);
    }

    private DashboardOverviewResponse.Chart buildChart(String selectedFarm, YearMonth currentMonth) {
        YearMonth startMonth = currentMonth.minusMonths(11);

        List<DashboardChartPointRow> rows = dashboardMapper.selectChartByMonthRange(
                selectedFarm,
                startMonth.toString(),
                currentMonth.toString()
        );

        Map<String, BigDecimal> valueMap = rows.stream()
                .collect(Collectors.toMap(
                        DashboardChartPointRow::getPeriod,
                        row -> nvl(row.getValue()),
                        (oldValue, newValue) -> newValue
                ));

        List<String> labels = new ArrayList<>();
        List<BigDecimal> values = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            YearMonth month = startMonth.plusMonths(i);

            String period = month.toString();

            String label = month.getMonth()
                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

            labels.add(label);
            values.add(valueMap.getOrDefault(period, BigDecimal.ZERO));
        }

        return new DashboardOverviewResponse.Chart(labels, values);
    }

    private DashboardOverviewResponse.Alert convertAlert(DashboardAlertRow row) {
        return new DashboardOverviewResponse.Alert(
                row.getId(),
                row.getTitle(),
                row.getEntity(),
                formatRelativeTime(row.getAlertTime()),
                row.getSeverity()
        );
    }

    private String formatRelativeTime(LocalDateTime alertTime) {
        if (alertTime == null) {
            return "unknown";
        }

        Duration duration = Duration.between(alertTime, LocalDateTime.now());

        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();

        if (minutes <= 0) {
            return "just now";
        }

        if (minutes < 60) {
            return minutes + " minutes ago";
        }

        if (hours < 24) {
            return hours + " hours ago";
        }

        return days + " days ago";
    }

    private String calculateTier(BigDecimal overallScore) {
        BigDecimal score = nvl(overallScore);

        if (score.compareTo(BigDecimal.valueOf(85)) >= 0) {
            return "Excellent";
        }

        if (score.compareTo(BigDecimal.valueOf(70)) >= 0) {
            return "Good";
        }

        if (score.compareTo(BigDecimal.valueOf(50)) >= 0) {
            return "Fair";
        }

        return "At Risk";
    }

    private DashboardStatsRow safeStats(DashboardStatsRow row) {
        if (row == null) {
            return emptyStats();
        }

        row.setOverall(nvl(row.getOverall()));
        row.setEnvironmental(nvl(row.getEnvironmental()));
        row.setSocial(nvl(row.getSocial()));
        row.setGovernance(nvl(row.getGovernance()));

        return row;
    }

    private DashboardStatsRow emptyStats() {
        DashboardStatsRow row = new DashboardStatsRow();
        row.setOverall(BigDecimal.ZERO);
        row.setEnvironmental(BigDecimal.ZERO);
        row.setSocial(BigDecimal.ZERO);
        row.setGovernance(BigDecimal.ZERO);
        return row;
    }

    private BigDecimal nvl(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        return value;
    }
}
