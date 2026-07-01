package com.farm2future.farm2future_backend.model.fram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TokenIssueRequest {

    @NotBlank(message = "crop_type is required")
    @JsonProperty("crop_type")
    private String cropType;

    @NotBlank(message = "batch_id is required")
    @JsonProperty("batch_id")
    private String batchId;

    @NotNull(message = "quantity_kg is required")
    @DecimalMin(value = "0.01", message = "quantity_kg must be greater than 0")
    @JsonProperty("quantity_kg")
    private BigDecimal quantityKg;
}