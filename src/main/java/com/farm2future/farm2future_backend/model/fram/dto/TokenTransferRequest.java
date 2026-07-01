package com.farm2future.farm2future_backend.model.fram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TokenTransferRequest {

    /**
     * token_id 可以和 URL 里的 tokenId 一致
     */
    @NotBlank(message = "token_id is required")
    @JsonProperty("token_id")
    private String tokenId;

    @NotBlank(message = "new_owner_address is required")
    @JsonProperty("new_owner_address")
    private String newOwnerAddress;
}
