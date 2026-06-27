package com.farm2future.farm2future_backend.model.dashboard.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardChartPointRow {
    private String period;

    private BigDecimal value;
}
