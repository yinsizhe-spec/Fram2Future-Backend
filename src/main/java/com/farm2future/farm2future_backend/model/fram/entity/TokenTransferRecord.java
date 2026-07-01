package com.farm2future.farm2future_backend.model.fram.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("token_transfer_record")
public class TokenTransferRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String tokenId;

    private String oldOwner;

    private String oldOwnerAddress;

    private String newOwner;

    private String newOwnerAddress;

    private String txHash;

    private LocalDateTime transferredAt;

    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
