package com.farm2future.farm2future_backend.model.fram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 农场数据提交响应 DTO
 *
 * <p>
 * 该类用于返回接口：
 * POST /api/farms/{farmId}/data
 * 的响应数据。
 * </p>
 *
 * <p>
 * 当前主要返回三部分内容：
 * 1. batch_id：本次提交生成的批次 ID
 * 2. tx_hash：区块链交易哈希，如果暂时未真正上链，也可以先返回模拟值
 * 3. submitted_at：数据提交时间
 * </p>
 */
@Data
@AllArgsConstructor
public class FarmDataSubmitResponse {

    /**
     * 批次 ID
     *
     * <p>
     * 对应 JSON 字段：batch_id
     * 用于唯一标识本次农场数据提交记录。
     * </p>
     *
     * <p>
     * 示例：batch_001
     * </p>
     */
    @JsonProperty("batch_id")
    private String batchId;

    /**
     * 区块链交易哈希
     *
     * <p>
     * 对应 JSON 字段：tx_hash
     * 用于表示该批次数据上链后的交易哈希。
     * </p>
     *
     * <p>
     * 如果当前阶段还没有真正接入智能合约，
     * 可以先返回一个模拟的交易哈希。
     * </p>
     *
     * <p>
     * 示例：0xabc123...
     * </p>
     */
    @JsonProperty("tx_hash")
    private String txHash;

    /**
     * 数据提交时间
     *
     * <p>
     * 对应 JSON 字段：submitted_at
     * 通常使用字符串格式返回时间。
     * </p>
     *
     * <p>
     * 示例：2026-06-29T18:30:00
     * </p>
     */
    @JsonProperty("submitted_at")
    private String submittedAt;
}