package com.farm2future.farm2future_backend.model.fram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 农场数据提交请求 DTO
 *
 * <p>
 * 该类用于接收前端提交到接口：
 * POST /api/farms/{farmId}/data
 * 的 JSON 请求数据。
 * </p>
 *
 * <p>
 * 主要包含两部分数据：
 * 1. batch：农作物批次、产量、销售、成本等业务数据
 * 2. iot_snapshot：IoT 设备采集的环境数据
 * </p>
 */
@Data
public class FarmDataSubmitRequest {

    /**
     * 农场 ID
     *
     * <p>
     * 对应前端 JSON 字段：farm_id
     * 例如：farm_1
     * </p>
     */
    @NotBlank(message = "farm_id is required")
    @JsonProperty("farm_id")
    private String farmId;

    /**
     * 农作物批次数据
     *
     * <p>
     * 包含作物类型、日期、产量、用水量、肥料使用量、销售数据和成本数据。
     * </p>
     *
     * <p>
     * @Valid 表示需要继续校验 Batch 内部字段上的校验注解。
     * </p>
     */
    @Valid
    @NotNull(message = "batch is required")
    private Batch batch;

    /**
     * IoT 环境快照数据
     *
     * <p>
     * 对应前端 JSON 字段：iot_snapshot
     * 包含土壤湿度、温度、空气湿度、PH 值等传感器数据。
     * </p>
     */
    @Valid
    @NotNull(message = "iot_snapshot is required")
    @JsonProperty("iot_snapshot")
    private IotSnapshot iotSnapshot;

    /**
     * 农作物批次数据内部类
     *
     * <p>
     * 用于接收 batch 对象中的数据。
     * </p>
     */
    @Data
    public static class Batch {

        /**
         * 作物类型
         *
         * <p>
         * 对应 JSON 字段：crop_type
         * 例如：Rice、Corn、Vegetables
         * </p>
         */
        @NotBlank(message = "crop_type is required")
        @JsonProperty("crop_type")
        private String cropType;

        /**
         * 数据记录日期
         *
         * <p>
         * JSON 格式建议使用：yyyy-MM-dd
         * 例如：2026-06-29
         * </p>
         */
        @NotNull(message = "date is required")
        private LocalDate date;

        /**
         * 农作物产量，单位：kg
         *
         * <p>
         * 对应 JSON 字段：yield_kg
         * 必须大于 0。
         * </p>
         */
        @NotNull(message = "yield_kg is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "yield_kg must be greater than 0")
        @JsonProperty("yield_kg")
        private BigDecimal yieldKg;

        /**
         * 用水量，单位：L
         *
         * <p>
         * 对应 JSON 字段：water_usage_l
         * 必须大于等于 0。
         * </p>
         */
        @NotNull(message = "water_usage_l is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "water_usage_l must be greater than or equal to 0")
        @JsonProperty("water_usage_l")
        private BigDecimal waterUsageL;

        /**
         * 肥料类型
         *
         * <p>
         * 对应 JSON 字段：fertiliser_type
         * 例如：Organic、Chemical、Mixed
         * </p>
         */
        @NotBlank(message = "fertiliser_type is required")
        @JsonProperty("fertiliser_type")
        private String fertiliserType;

        /**
         * 肥料使用量，单位：kg
         *
         * <p>
         * 对应 JSON 字段：fertiliser_usage_kg
         * 必须大于等于 0。
         * </p>
         */
        @NotNull(message = "fertiliser_usage_kg is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "fertiliser_usage_kg must be greater than or equal to 0")
        @JsonProperty("fertiliser_usage_kg")
        private BigDecimal fertiliserUsageKg;

        /**
         * 销售数量，单位：kg
         *
         * <p>
         * 对应 JSON 字段：sale_quantity_kg
         * 必须大于等于 0。
         * </p>
         */
        @NotNull(message = "sale_quantity_kg is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "sale_quantity_kg must be greater than or equal to 0")
        @JsonProperty("sale_quantity_kg")
        private BigDecimal saleQuantityKg;

        /**
         * 销售单价，单位：RM/kg
         *
         * <p>
         * 对应 JSON 字段：sale_unit_price_rm
         * 必须大于等于 0。
         * </p>
         */
        @NotNull(message = "sale_unit_price_rm is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "sale_unit_price_rm must be greater than or equal to 0")
        @JsonProperty("sale_unit_price_rm")
        private BigDecimal saleUnitPriceRm;

        /**
         * 买家名称
         *
         * <p>
         * 对应 JSON 字段：buyer_name
         * 例如：Green Market Sdn Bhd
         * </p>
         */
        @NotBlank(message = "buyer_name is required")
        @JsonProperty("buyer_name")
        private String buyerName;

        /**
         * 种子成本，单位：RM
         *
         * <p>
         * 对应 JSON 字段：seed_cost_rm
         * 必须大于等于 0。
         * </p>
         */
        @NotNull(message = "seed_cost_rm is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "seed_cost_rm must be greater than or equal to 0")
        @JsonProperty("seed_cost_rm")
        private BigDecimal seedCostRm;

        /**
         * 肥料成本，单位：RM
         *
         * <p>
         * 对应 JSON 字段：fertiliser_cost_rm
         * 必须大于等于 0。
         * </p>
         */
        @NotNull(message = "fertiliser_cost_rm is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "fertiliser_cost_rm must be greater than or equal to 0")
        @JsonProperty("fertiliser_cost_rm")
        private BigDecimal fertiliserCostRm;
    }

    /**
     * IoT 环境数据快照内部类
     *
     * <p>
     * 用于接收 iot_snapshot 对象中的传感器数据。
     * </p>
     */
    @Data
    public static class IotSnapshot {

        /**
         * 土壤湿度百分比
         *
         * <p>
         * 对应 JSON 字段：soil_moisture_pct
         * 例如：65.5
         * </p>
         */
        @NotNull(message = "soil_moisture_pct is required")
        @JsonProperty("soil_moisture_pct")
        private BigDecimal soilMoisturePct;

        /**
         * 温度，单位：摄氏度 ℃
         *
         * <p>
         * 对应 JSON 字段：temperature_c
         * 例如：28.6
         * </p>
         */
        @NotNull(message = "temperature_c is required")
        @JsonProperty("temperature_c")
        private BigDecimal temperatureC;

        /**
         * 空气湿度百分比
         *
         * <p>
         * 对应 JSON 字段：humidity_pct
         * 例如：78.2
         * </p>
         */
        @NotNull(message = "humidity_pct is required")
        @JsonProperty("humidity_pct")
        private BigDecimal humidityPct;

        /**
         * 土壤 PH 值
         *
         * <p>
         * 对应 JSON 字段：ph_level
         * 例如：6.8
         * </p>
         */
        @NotNull(message = "ph_level is required")
        @JsonProperty("ph_level")
        private BigDecimal phLevel;
    }
}