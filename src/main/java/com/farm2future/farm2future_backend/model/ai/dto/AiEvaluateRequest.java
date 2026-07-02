package com.farm2future.farm2future_backend.model.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AiEvaluateRequest {

    @JsonProperty("farm_id")
    private String farmId;

    private String period;

    @JsonProperty("aggregated_features")
    private AggregatedFeatures aggregatedFeatures;

    @Data
    public static class AggregatedFeatures {

        @JsonProperty("resource_efficiency")
        private BigDecimal resourceEfficiency;

        @JsonProperty("chemical_compliance")
        private BigDecimal chemicalCompliance;

        @JsonProperty("labor_equity_score")
        private BigDecimal laborEquityScore;

        @JsonProperty("supply_chain_trans")
        private BigDecimal supplyChainTrans;

        @JsonProperty("compliance_stability")
        private BigDecimal complianceStability;

        @JsonProperty("system_integrity")
        private BigDecimal systemIntegrity;
    }
}