package com.farm2future.farm2future_backend.model.fram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FarmDataSubmitResponse {
    @JsonProperty("batch_id")
    private String batchId;

    @JsonProperty("tx_hash")
    private String txHash;

    @JsonProperty("submitted_at")
    private String submittedAt;
}
