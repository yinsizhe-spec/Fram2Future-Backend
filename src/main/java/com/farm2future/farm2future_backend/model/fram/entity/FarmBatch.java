package com.farm2future.farm2future_backend.model.fram.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("farm_batch")
public class FarmBatch {
    @TableId(type = IdType.INPUT)
    private String id;

    private String farmId;

    private String cropType;

    private LocalDate batchDate;

    private BigDecimal yieldKg;

    private BigDecimal waterUsageL;

    private String fertiliserType;

    private BigDecimal fertiliserUsageKg;

    private BigDecimal saleQuantityKg;

    private BigDecimal saleUnitPriceRm;

    private String buyerName;

    private BigDecimal seedCostRm;

    private BigDecimal fertiliserCostRm;

    private String txHash;

    private LocalDateTime submittedAt;

    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
