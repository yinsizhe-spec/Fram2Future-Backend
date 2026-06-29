package com.farm2future.farm2future_backend.model.fram.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FarmDataSubmitRequest {
    @NotBlank(message = "farm_id is required")
    @JsonProperty("farm_id")
    private String farmId;

    @Valid
    @NotNull(message = "batch is required")
    private Batch batch;

    @Valid
    @NotNull(message = "iot_snapshot is required")
    @JsonProperty("iot_snapshot")
    private IotSnapshot iotSnapshot;

    @Data
    public static class Batch {

        @NotBlank(message = "crop_type is required")
        @JsonProperty("crop_type")
        private String cropType;

        @NotNull(message = "date is required")
        private LocalDate date;

        @NotNull(message = "yield_kg is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "yield_kg must be greater than 0")
        @JsonProperty("yield_kg")
        private BigDecimal yieldKg;

        @NotNull(message = "water_usage_l is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "water_usage_l must be greater than or equal to 0")
        @JsonProperty("water_usage_l")
        private BigDecimal waterUsageL;

        @NotBlank(message = "fertiliser_type is required")
        @JsonProperty("fertiliser_type")
        private String fertiliserType;

        @NotNull(message = "fertiliser_usage_kg is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "fertiliser_usage_kg must be greater than or equal to 0")
        @JsonProperty("fertiliser_usage_kg")
        private BigDecimal fertiliserUsageKg;

        @NotNull(message = "sale_quantity_kg is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "sale_quantity_kg must be greater than or equal to 0")
        @JsonProperty("sale_quantity_kg")
        private BigDecimal saleQuantityKg;

        @NotNull(message = "sale_unit_price_rm is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "sale_unit_price_rm must be greater than or equal to 0")
        @JsonProperty("sale_unit_price_rm")
        private BigDecimal saleUnitPriceRm;

        @NotBlank(message = "buyer_name is required")
        @JsonProperty("buyer_name")
        private String buyerName;

        @NotNull(message = "seed_cost_rm is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "seed_cost_rm must be greater than or equal to 0")
        @JsonProperty("seed_cost_rm")
        private BigDecimal seedCostRm;

        @NotNull(message = "fertiliser_cost_rm is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "fertiliser_cost_rm must be greater than or equal to 0")
        @JsonProperty("fertiliser_cost_rm")
        private BigDecimal fertiliserCostRm;
    }

    @Data
    public static class IotSnapshot {

        @NotNull(message = "soil_moisture_pct is required")
        @JsonProperty("soil_moisture_pct")
        private BigDecimal soilMoisturePct;

        @NotNull(message = "temperature_c is required")
        @JsonProperty("temperature_c")
        private BigDecimal temperatureC;

        @NotNull(message = "humidity_pct is required")
        @JsonProperty("humidity_pct")
        private BigDecimal humidityPct;

        @NotNull(message = "ph_level is required")
        @JsonProperty("ph_level")
        private BigDecimal phLevel;
    }
}
