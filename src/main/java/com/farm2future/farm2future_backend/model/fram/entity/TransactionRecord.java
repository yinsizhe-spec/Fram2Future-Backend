package com.farm2future.farm2future_backend.model.fram.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("transaction_record")
public class TransactionRecord {
    private Long id;

    private Long farmId;

    private String transactionType;

    private BigDecimal amount;

    private String description;

    private LocalDateTime createdAt;

    private Integer deleted;
}
