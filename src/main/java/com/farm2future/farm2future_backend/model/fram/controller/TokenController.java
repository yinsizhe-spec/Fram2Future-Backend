package com.farm2future.farm2future_backend.model.fram.controller;

import com.farm2future.farm2future_backend.model.fram.dto.TokenIssueRequest;
import com.farm2future.farm2future_backend.model.fram.dto.TokenIssueResponse;
import com.farm2future.farm2future_backend.model.fram.dto.TokenPageResponse;
import com.farm2future.farm2future_backend.model.fram.dto.TokenTransferHistoryResponse;
import com.farm2future.farm2future_backend.model.fram.dto.TokenTransferRecordResponse;
import com.farm2future.farm2future_backend.model.fram.dto.TokenTransferRequest;
import com.farm2future.farm2future_backend.model.fram.dto.TokenTransferResponse;
import com.farm2future.farm2future_backend.model.fram.service.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Token 接口 Controller。
 */
@RestController
@RequestMapping("/api/tokens")
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;

    /**
     * GET /api/tokens
     *
     * 查询 Token 列表，分页返回。
     *
     * 示例：
     * GET /api/tokens
     * GET /api/tokens?status=normal&page=1&size=20
     * GET /api/tokens?search=Green Valley Farm
     */
    @GetMapping
    public TokenPageResponse listTokens(
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        return tokenService.listTokens(status, search, page, size);
    }

    /**
     * POST /api/tokens
     *
     * 根据 batch_id 发行 Token。
     */
    @PostMapping
    public TokenIssueResponse issue(
            @Valid @RequestBody TokenIssueRequest request
    ) {
        return tokenService.issue(request);
    }

    /**
     * POST /api/tokens/{tokenId}/transfer
     *
     * 转移 Token。
     */
    @PostMapping("/{tokenId}/transfer")
    public TokenTransferResponse transfer(
            @PathVariable String tokenId,
            @Valid @RequestBody TokenTransferRequest request
    ) {
        return tokenService.transfer(tokenId, request);
    }

    /**
     * GET /api/tokens/transfers
     *
     * 查询全部 Token 转账记录。
     * 如果传 farmId，则查询某个农场的转账记录。
     */
    @GetMapping("/transfers")
    public List<TokenTransferRecordResponse> listTransferRecords(
            @RequestParam(required = false) String farmId
    ) {
        return tokenService.listTransferRecords(farmId);
    }

    /**
     * GET /api/tokens/{tokenId}/transfers
     *
     * 查询某个 Token 的转账历史。
     */
    @GetMapping("/{tokenId}/transfers")
    public List<TokenTransferHistoryResponse> listTransferHistory(
            @PathVariable String tokenId
    ) {
        return tokenService.listTransferHistory(tokenId);
    }
}