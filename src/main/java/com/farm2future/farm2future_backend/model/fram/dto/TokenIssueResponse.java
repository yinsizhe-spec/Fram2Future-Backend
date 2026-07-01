package com.farm2future.farm2future_backend.model.fram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Token 发行返回 DTO。
 */
@Data
public class TokenIssueResponse {

    @JsonProperty("token_id")
    private String tokenId;

    @JsonProperty("tx_hash")
    private String txHash;
}