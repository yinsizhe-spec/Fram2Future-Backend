package com.farm2future.farm2future_backend.model.fram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenTransferResponse {

    @JsonProperty("tx_hash")
    private String txHash;

    @JsonProperty("transferred_at")
    private LocalDateTime transferredAt;
}
