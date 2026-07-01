package com.farm2future.farm2future_backend.model.fram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Token 转移返回 DTO。
 *
 * 对应 POST /api/tokens/{tokenId}/transfer 返回结果。
 */
@Data
public class TokenTransferResponse {

    /**
     * 区块链交易 Hash
     */
    @JsonProperty("tx_hash")
    private String txHash;

    /**
     * 转移时间，返回给前端 ISO 字符串
     *
     * 示例：
     * 2026-07-01T10:30:00Z
     */
    @JsonProperty("transferred_at")
    private String transferredAt;
}