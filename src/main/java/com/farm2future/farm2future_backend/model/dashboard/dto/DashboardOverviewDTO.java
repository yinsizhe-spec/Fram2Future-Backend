package com.farm2future.farm2future_backend.model.dashboard.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardOverviewDTO {
    private Long totalFarms;
    private BigDecimal totalTokens;
    private Long totalTransactions;
    private BigDecimal averageEsgScore;
}
