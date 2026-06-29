package com.farm2future.farm2future_backend.model.fram.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("transaction_record")
public class TransactionRecord {
    @TableId(type = IdType.INPUT)
    private String id;

    private String tokenId;

    private String fromParty;

    private String toParty;

    private String txType;

    private String txHash;

    private LocalDateTime txDate;

    private String status;

    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
