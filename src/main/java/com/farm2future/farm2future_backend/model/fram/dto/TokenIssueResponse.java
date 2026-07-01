package com.farm2future.farm2future_backend.model.fram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenIssueResponse {

    @JsonProperty("token_id")
    private String tokenId;

    @JsonProperty("tx_hash")
    private String txHash;
}