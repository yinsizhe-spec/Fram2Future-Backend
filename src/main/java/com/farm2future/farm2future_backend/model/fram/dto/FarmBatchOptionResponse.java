package com.farm2future.farm2future_backend.model.fram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FarmBatchOptionResponse {
    @JsonProperty("batch_id")
    private String batchId;

    @JsonProperty("farm_id")
    private String farmId;

    @JsonProperty("farm_name")
    private String farmName;

    @JsonProperty("crop_type")
    private String cropType;

    @JsonProperty("batch_date")
    private LocalDate batchDate;

    @JsonProperty("yield_kg")
    private BigDecimal yieldKg;

    @JsonProperty("available_quantity_kg")
    private BigDecimal availableQuantityKg;
}
