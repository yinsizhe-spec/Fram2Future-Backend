package com.farm2future.farm2future_backend.model.fram.controller;

import com.farm2future.farm2future_backend.model.fram.dto.TokenIssueRequest;
import com.farm2future.farm2future_backend.model.fram.dto.TokenIssueResponse;
import com.farm2future.farm2future_backend.model.fram.service.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tokens")
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;

    /**
     * POST /api/tokens
     *
     * 发行一个新的农产品 Token
     */
    @PostMapping
    public TokenIssueResponse issue(@Valid @RequestBody TokenIssueRequest request) {
        return tokenService.issue(request);
    }
}