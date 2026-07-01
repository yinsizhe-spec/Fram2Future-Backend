package com.farm2future.farm2future_backend.model.fram.dto;

import lombok.Data;

/**
 * Token 列表单条返回对象。
 */
@Data
public class TokenListResponse {

    private String id;

    private String asset;

    private String owner;

    private String status;

    /**
     * ISO 时间字符串，例如 2026-07-01T10:30:00Z
     */
    private String date;
}