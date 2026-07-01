package com.farm2future.farm2future_backend.model.fram.controller;

import com.farm2future.farm2future_backend.model.fram.dto.TokenIssueRequest;
import com.farm2future.farm2future_backend.model.fram.dto.TokenIssueResponse;
import com.farm2future.farm2future_backend.model.fram.dto.TokenListResponse;
import com.farm2future.farm2future_backend.model.fram.dto.TokenTransferHistoryResponse;
import com.farm2future.farm2future_backend.model.fram.dto.TokenTransferRecordResponse;
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
     * GET /api/tokens/transfers
     *
     * 查询全部或某个农场的 Token 转账记录
     */
    @GetMapping("/transfers")
    public List<TokenTransferRecordResponse> listTransferRecords(
            @RequestParam(required = false) String farmId
    ) {
        return tokenService.listTransferRecords(farmId);
    }

    /**
     * POST /api/tokens
     */
    @PostMapping
    public TokenIssueResponse issue(@Valid @RequestBody TokenIssueRequest request) {
        return tokenService.issue(request);
    }

    /**
     * POST /api/tokens/{tokenId}/transfer
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
     * 查询某一个 Token 的转账历史
     */
    @GetMapping("/{tokenId}/transfers")
    public List<TokenTransferHistoryResponse> listTransferHistory(
            @PathVariable String tokenId
    ) {
        return tokenService.listTransferHistory(tokenId);
    }
}