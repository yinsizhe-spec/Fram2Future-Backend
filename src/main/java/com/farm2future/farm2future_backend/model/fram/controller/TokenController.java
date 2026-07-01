package com.farm2future.farm2future_backend.model.fram.controller;

import com.farm2future.farm2future_backend.model.fram.dto.TokenIssueRequest;
import com.farm2future.farm2future_backend.model.fram.dto.TokenIssueResponse;
import com.farm2future.farm2future_backend.model.fram.dto.TokenListResponse;
import com.farm2future.farm2future_backend.model.fram.dto.TokenTransferHistoryResponse;
import com.farm2future.farm2future_backend.model.fram.dto.TokenTransferRequest;
import com.farm2future.farm2future_backend.model.fram.dto.TokenTransferResponse;
import com.farm2future.farm2future_backend.model.fram.service.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tokens")
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;

    /**
     * GET /api/tokens
     *
     * 查询 Token 列表
     */
    @GetMapping
    public List<TokenListResponse> listTokens(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String owner,
            @RequestParam(required = false) String cropType
    ) {
        return tokenService.listTokens(status, owner, cropType);
    }

    /**
     * POST /api/tokens
     *
     * 发行 Token
     */
    @PostMapping
    public TokenIssueResponse issue(@Valid @RequestBody TokenIssueRequest request) {
        return tokenService.issue(request);
    }

    /**
     * POST /api/tokens/{tokenId}/transfer
     *
     * 转移 Token
     */
    @PostMapping("/{tokenId}/transfer")
    public TokenTransferResponse transfer(
            @PathVariable String tokenId,
            @Valid @RequestBody TokenTransferRequest request
    ) {
        return tokenService.transfer(tokenId, request);
    }

    /**
     * GET /api/tokens/{tokenId}/transfers
     *
     * 查询某个 Token 的转移历史
     */
    @GetMapping("/{tokenId}/transfers")
    public List<TokenTransferHistoryResponse> listTransferHistory(
            @PathVariable String tokenId
    ) {
        return tokenService.listTransferHistory(tokenId);
    }
}