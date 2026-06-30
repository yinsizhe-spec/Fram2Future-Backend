package com.farm2future.farm2future_backend.model.report.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class EsgReportGenerateResponse {
    private Period period;

    private String entity;

    private List<ScoreItem> scores;

    @JsonProperty("risk_flags")
    private List<RiskFlag> riskFlags;

    @JsonProperty("generated_at")
    private String generatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Period {
        private String from;
        private String to;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoreItem {
        private String label;
        private Integer score;
        private String note;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskFlag {
        private String type;
        private String title;
        private String desc;
    }
}
