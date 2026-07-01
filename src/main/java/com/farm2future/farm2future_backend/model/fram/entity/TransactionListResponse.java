package com.farm2future.farm2future_backend.model.fram.entity;

import lombok.Data;

import java.util.List;

/**
 * 交易记录分页返回对象。
 *
 * 对应前端 API_CONTRACT.md:
 *
 * {
 *   "items": [],
 *   "total": 8,
 *   "page": 1,
 *   "size": 20
 * }
 */
@Data
public class TransactionListResponse {

    /**
     * 当前页数据
     */
    private List<TransactionItemResponse> items;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer page;

    /**
     * 每页数量
     */
    private Integer size;
}
