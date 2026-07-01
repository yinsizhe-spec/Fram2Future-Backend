package com.farm2future.farm2future_backend.model.fram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TokenTransferHistoryResponse {

    private Long id;

    @JsonProperty("token_id")
    private String tokenId;

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
    private LocalDateTime transferredAt;
}