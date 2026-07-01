package com.farm2future.farm2future_backend.model.fram.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * token_record 表实体类
 */
@Data
@TableName("token_record")
public class TokenRecord {

    @TableId(type = IdType.INPUT)
    private String id;

    private String batchId;

    private String farmId;

    private String cropType;

    private String asset;

    private BigDecimal quantityKg;

    private BigDecimal tokenAmount;

    private String owner;

    private String ownerAddress;

    /**
     * normal / flagged / at-risk
     */
    private String status;

    private String txHash;

    private LocalDateTime issueDate;

    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
