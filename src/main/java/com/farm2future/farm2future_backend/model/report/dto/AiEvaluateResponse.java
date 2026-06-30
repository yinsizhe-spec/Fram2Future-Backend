package com.farm2future.farm2future_backend.model.report.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AiEvaluateResponse {

    @JsonProperty("farm_id")
    private String farmId;

    private String period;

    @JsonProperty("anomaly_detected")
    private Boolean anomalyDetected;

    @JsonProperty("esg_compliant")
    private Boolean esgCompliant;

    @JsonProperty("final_esg_score")
    private BigDecimal finalEsgScore;

    @JsonProperty("esg_grade")
    private String esgGrade;

    @JsonProperty("suggested_token_amount")
    private BigDecimal suggestedTokenAmount;

    @JsonProperty("xai_explanation")
    private String xaiExplanation;
}