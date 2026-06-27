package com.farm2future.farm2future_backend.model.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewResponse {
    private String tier;
    private Stats stats;
    private Chart chart;
    private List<Alert> alerts;
    private List<String> farms;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stats {
        private BigDecimal overall;
        private BigDecimal environmental;
        private BigDecimal social;
        private BigDecimal governance;
        private Changes changes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Changes {
        private BigDecimal overall;
        private BigDecimal environmental;
        private BigDecimal social;
        private BigDecimal governance;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Chart {
        private List<String> labels;
        private List<BigDecimal> values;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Alert {
        private Long id;
        private String title;
        private String entity;
        private String time;
        private String severity;
    }
}
