package com.farm2future.farm2future_backend.model.fram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Token 转账记录返回 DTO。
 */
@Data
public class TokenTransferRecordResponse {

    private Long id;

    @JsonProperty("token_id")
    private String tokenId;

    @JsonProperty("farm_id")
    private String farmId;

    private String asset;

    @JsonProperty("crop_type")
    private String cropType;

    @JsonProperty("old_owner")
    private String oldOwner;

    @JsonProperty("old_owner_address")
    private String oldOwnerAddress;

    @JsonProperty("new_owner")
    private String newOwner;

    @JsonProperty("new_owner_address")
    private String newOwnerAddress;

    @JsonProperty("tx_hash")
    private String txHash;

    @JsonProperty("transferred_at")
    private String transferredAt;
}