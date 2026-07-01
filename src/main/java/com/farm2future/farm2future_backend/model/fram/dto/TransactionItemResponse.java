package com.farm2future.farm2future_backend.model.fram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 单条交易记录返回对象。
 *
 * 对应前端 API_CONTRACT.md:
 *
 * {
 *   "id": "TXN-2024-101",
 *   "token": "TKN-2024-001",
 *   "from": "System",
 *   "to": "Green Valley Farm",
 *   "date": "2024-10-12T09:30:00Z",
 *   "status": "completed"
 * }
 */
@Data
public class TransactionItemResponse {

    /**
     * 交易 ID
     */
    private String id;

    /**
     * Token ID
     *
     * 注意：
     * 前端字段名要求是 token，不是 tokenId。
     */
    private String token;

    /**
     * 转出方
     *
     * 注意：
     * JSON 字段名需要返回 from。
     */
    @JsonProperty("from")
    private String fromParty;

    /**
     * 接收方
     *
     * 注意：
     * JSON 字段名需要返回 to。
     */
    @JsonProperty("to")
    private String toParty;

    /**
     * 交易时间，ISO-8601 字符串
     */
    private String date;

    /**
     * 交易状态
     */
    private String status;
}