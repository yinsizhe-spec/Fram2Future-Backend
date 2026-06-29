package com.farm2future.farm2future_backend.model.fram.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("iot_snapshot")
public class IotSnapshot {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String batchId;

    private String farmId;

    private BigDecimal soilMoisturePct;

    private BigDecimal temperatureC;

    private BigDecimal humidityPct;

    private BigDecimal phLevel;

    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
