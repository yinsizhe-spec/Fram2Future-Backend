package com.farm2future.farm2future_backend.model.fram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TokenListResponse {

    @JsonProperty("token_id")
    private String tokenId;

    @JsonProperty("batch_id")
    private String batchId;

    @JsonProperty("farm_id")
    private String farmId;

    @JsonProperty("crop_type")
    private String cropType;

    private String asset;

    @JsonProperty("quantity_kg")
    private BigDecimal quantityKg;

    @JsonProperty("token_amount")
    private BigDecimal tokenAmount;

    private String owner;

    @JsonProperty("owner_address")
    private String ownerAddress;

    private String status;

    @JsonProperty("tx_hash")
    private String txHash;

    @JsonProperty("issue_date")
    private LocalDateTime issueDate;
}