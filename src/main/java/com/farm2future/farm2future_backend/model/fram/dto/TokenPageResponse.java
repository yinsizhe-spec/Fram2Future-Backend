package com.farm2future.farm2future_backend.model.fram.dto;

import lombok.Data;

import java.util.List;

/**
 * Token 分页返回 DTO。
 */
@Data
public class TokenPageResponse {

    private List<TokenListResponse> items;

    private Long total;

    private Integer page;

    private Integer size;
}