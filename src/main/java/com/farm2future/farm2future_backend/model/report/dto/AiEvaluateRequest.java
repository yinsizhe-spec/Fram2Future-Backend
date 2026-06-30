package com.farm2future.farm2future_backend.model.report.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class AiEvaluateRequest {
    @JsonProperty("farm_id")
    private String farmId;

    private String period;

    @JsonProperty("aggregated_features")
    private Map<String, BigDecimal> aggregatedFeatures;
}
