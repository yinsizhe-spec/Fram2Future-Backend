package com.farm2future.farm2future_backend.model.fram.controller;

import com.farm2future.farm2future_backend.model.fram.entity.TransactionListResponse;
import com.farm2future.farm2future_backend.model.fram.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 交易记录 Controller。
 *
 * 对应前端 API_CONTRACT.md:
 *
 * GET /api/transactions
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * GET /api/transactions
     *
     * 查询所有区块链交易记录。
     *
     * 请求示例：
     *
     * GET /api/transactions
     * GET /api/transactions?page=1&size=20
     * GET /api/transactions?search=TKN-2026-00001
     * GET /api/transactions?date=2026-07-01
     * GET /api/transactions?search=Green Valley Farm&page=1&size=20
     *
     * @param search 搜索关键词，可选
     * @param date   日期过滤，可选，格式 yyyy-MM-dd
     * @param page   页码，默认 1
     * @param size   每页数量，默认 20
     * @return 交易记录分页数据
     */
    @GetMapping
    public TransactionListResponse listTransactions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        return transactionService.listTransactions(search, date, page, size);
    }
}