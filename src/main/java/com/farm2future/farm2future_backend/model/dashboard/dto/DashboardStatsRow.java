package com.farm2future.farm2future_backend.model.dashboard.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardStatsRow {
    private BigDecimal overall;

    private BigDecimal environmental;

    private BigDecimal social;

    private BigDecimal governance;
}
