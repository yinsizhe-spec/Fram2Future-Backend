package com.farm2future.farm2future_backend.model.fram.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("token_record")
public class TokenRecord {
    private Long id;

    private Long farmId;

    private BigDecimal tokenAmount;

    private String reason;

    private LocalDateTime createdAt;

    private Integer deleted;
}
