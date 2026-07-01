package com.farm2future.farm2future_backend.model.fram.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.farm2future.farm2future_backend.model.fram.dto.TransactionItemResponse;
import com.farm2future.farm2future_backend.model.fram.dto.TransactionListResponse;
import com.farm2future.farm2future_backend.model.fram.entity.TransactionRecord;
import com.farm2future.farm2future_backend.model.fram.mapper.TransactionRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 交易记录 Service。
 *
 * 实现 GET /api/transactions 接口的业务逻辑。
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRecordMapper transactionRecordMapper;

    /**
     * 查询交易记录列表。
     *
     * 支持前端 API_CONTRACT.md 中要求的参数：
     *
     * search: 搜索 id、token、from、to
     * date: 按日期过滤，例如 2026-07-01
     * page: 页码，默认 1
     * size: 每页数量，默认 20
     *
     * @param search 搜索关键词
     * @param date   日期过滤，格式 yyyy-MM-dd
     * @param page   页码
     * @param size   每页数量
     * @return 分页交易记录
     */
    public TransactionListResponse listTransactions(
            String search,
            String date,
            Integer page,
            Integer size
    ) {
        int pageNo = normalizePage(page);
        int pageSize = normalizeSize(size);

        Page<TransactionRecord> pageParam = new Page<>(pageNo, pageSize);

        LambdaQueryWrapper<TransactionRecord> wrapper = new LambdaQueryWrapper<>();

        /*
         * 只查询未删除数据。
         */
        wrapper.eq(TransactionRecord::getDeleted, 0);

        /*
         * search 支持搜索：
         * id
         * token_id
         * from_party
         * to_party
         */
        if (StringUtils.hasText(search)) {
            String keyword = search.trim();

            wrapper.and(w -> w
                    .like(TransactionRecord::getId, keyword)
                    .or()
                    .like(TransactionRecord::getTokenId, keyword)
                    .or()
                    .like(TransactionRecord::getFromParty, keyword)
                    .or()
                    .like(TransactionRecord::getToParty, keyword)
            );
        }

        /*
         * date 按日期过滤。
         *
         * 前端传：
         * 2026-07-01
         *
         * 后端转换成：
         * tx_date >= 2026-07-01 00:00:00
         * tx_date <  2026-07-02 00:00:00
         */
        if (StringUtils.hasText(date)) {
            LocalDate localDate = parseDate(date.trim());
            LocalDateTime start = localDate.atStartOfDay();
            LocalDateTime end = localDate.plusDays(1).atStartOfDay();

            wrapper.ge(TransactionRecord::getTxDate, start);
            wrapper.lt(TransactionRecord::getTxDate, end);
        }

        /*
         * 最新交易排在最前面。
         */
        wrapper.orderByDesc(TransactionRecord::getTxDate);

        Page<TransactionRecord> resultPage = transactionRecordMapper.selectPage(pageParam, wrapper);

        List<TransactionItemResponse> items = resultPage.getRecords()
                .stream()
                .map(this::toTransactionItemResponse)
                .toList();

        TransactionListResponse response = new TransactionListResponse();
        response.setItems(items);
        response.setTotal(resultPage.getTotal());
        response.setPage(pageNo);
        response.setSize(pageSize);

        return response;
    }

    /**
     * 转换数据库实体为前端需要的返回格式。
     *
     * @param record 数据库交易记录
     * @return 前端交易记录对象
     */
    private TransactionItemResponse toTransactionItemResponse(TransactionRecord record) {
        TransactionItemResponse item = new TransactionItemResponse();

        item.setId(record.getId());
        item.setToken(record.getTokenId());
        item.setFromParty(record.getFromParty());
        item.setToParty(record.getToParty());
        item.setStatus(record.getStatus());

        if (record.getTxDate() != null) {
            /*
             * 前端 API 文档要求 datetime 使用 ISO-8601，并带 Z。
             * 数据库里是 LocalDateTime，没有时区信息。
             * 这里按 UTC 输出，例如：2026-07-01T10:30:00Z
             */
            String isoDate = record.getTxDate()
                    .atOffset(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ISO_INSTANT);

            item.setDate(isoDate);
        }

        return item;
    }

    /**
     * 规范化页码。
     *
     * @param page 前端传入页码
     * @return 合法页码
     */
    private int normalizePage(Integer page) {
        if (page == null || page < 1) {
            return 1;
        }
        return page;
    }

    /**
     * 规范化每页数量。
     *
     * @param size 前端传入每页数量
     * @return 合法每页数量
     */
    private int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return 20;
        }

        /*
         * 防止前端一次请求太多数据。
         */
        return Math.min(size, 100);
    }

    /**
     * 解析日期。
     *
     * @param date 日期字符串，例如 2026-07-01
     * @return LocalDate
     */
    private LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            throw new IllegalArgumentException("date must be ISO format yyyy-MM-dd, for example 2026-07-01");
        }
    }
}
