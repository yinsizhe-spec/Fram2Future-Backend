package com.farm2future.farm2future_backend.model.fram.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * IoT 环境数据快照实体类
 *
 * <p>
 * 该类对应数据库中的 iot_snapshot 表。
 * 用于保存农场某一次批次提交时，
 * IoT 设备采集到的环境数据，例如土壤湿度、温度、空气湿度、PH 值等。
 * </p>
 */
@Data
@TableName("iot_snapshot")
public class IotSnapshot {

    /**
     * IoT 快照记录 ID
     *
     * <p>
     * 对应数据库字段：id
     * 当前使用 IdType.AUTO，表示数据库自动递增生成主键。
     * </p>
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 批次 ID
     *
     * <p>
     * 对应数据库字段：batch_id
     * 用于关联 farm_batch 表中的某一次农场批次数据。
     * </p>
     */
    private String batchId;

    /**
     * 农场 ID
     *
     * <p>
     * 对应数据库字段：farm_id
     * 用于标识该 IoT 数据属于哪个农场。
     * </p>
     */
    private String farmId;

    /**
     * 土壤湿度百分比
     *
     * <p>
     * 对应数据库字段：soil_moisture_pct
     * 例如：65.50
     * </p>
     */
    private BigDecimal soilMoisturePct;

    /**
     * 温度，单位：摄氏度 ℃
     *
     * <p>
     * 对应数据库字段：temperature_c
     * 例如：28.60
     * </p>
     */
    private BigDecimal temperatureC;

    /**
     * 空气湿度百分比
     *
     * <p>
     * 对应数据库字段：humidity_pct
     * 例如：78.20
     * </p>
     */
    private BigDecimal humidityPct;

    /**
     * 土壤 PH 值
     *
     * <p>
     * 对应数据库字段：ph_level
     * 例如：6.80
     * </p>
     */
    private BigDecimal phLevel;

    /**
     * 逻辑删除标记
     *
     * <p>
     * 对应数据库字段：deleted
     * 一般约定：
     * 0 表示未删除
     * 1 表示已删除
     * </p>
     */
    private Integer deleted;

    /**
     * 创建时间
     *
     * <p>
     * 对应数据库字段：create_time
     * 记录该 IoT 快照数据第一次创建的时间。
     * </p>
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     *
     * <p>
     * 对应数据库字段：update_time
     * 记录该 IoT 快照数据最近一次修改的时间。
     * </p>
     */
    private LocalDateTime updateTime;
}