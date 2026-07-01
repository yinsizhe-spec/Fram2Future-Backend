package com.farm2future.farm2future_backend.model.fram.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.farm2future.farm2future_backend.model.fram.dto.TokenIssueRequest;
import com.farm2future.farm2future_backend.model.fram.dto.TokenIssueResponse;
import com.farm2future.farm2future_backend.model.fram.dto.TokenListResponse;
import com.farm2future.farm2future_backend.model.fram.dto.TokenPageResponse;
import com.farm2future.farm2future_backend.model.fram.dto.TokenTransferHistoryResponse;
import com.farm2future.farm2future_backend.model.fram.dto.TokenTransferRecordResponse;
import com.farm2future.farm2future_backend.model.fram.dto.TokenTransferRequest;
import com.farm2future.farm2future_backend.model.fram.dto.TokenTransferResponse;
import com.farm2future.farm2future_backend.model.fram.entity.Farm;
import com.farm2future.farm2future_backend.model.fram.entity.FarmBatch;
import com.farm2future.farm2future_backend.model.fram.entity.TokenRecord;
import com.farm2future.farm2future_backend.model.fram.entity.TokenTransferRecord;
import com.farm2future.farm2future_backend.model.fram.entity.TransactionRecord;
import com.farm2future.farm2future_backend.model.fram.mapper.FarmBatchMapper;
import com.farm2future.farm2future_backend.model.fram.mapper.FarmMapper;
import com.farm2future.farm2future_backend.model.fram.mapper.TokenRecordMapper;
import com.farm2future.farm2future_backend.model.fram.mapper.TokenTransferRecordMapper;
import com.farm2future.farm2future_backend.model.fram.mapper.TransactionRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Token 业务 Service。
 */
@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRecordMapper tokenRecordMapper;
    private final TokenTransferRecordMapper tokenTransferRecordMapper;
    private final TransactionRecordMapper transactionRecordMapper;
    private final FarmBatchMapper farmBatchMapper;
    private final FarmMapper farmMapper;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * GET /api/tokens
     *
     * 查询 Token 列表，分页返回。
     */
    public TokenPageResponse listTokens(
            String status,
            String search,
            Integer page,
            Integer size
    ) {
        int pageNo = normalizePage(page);
        int pageSize = normalizeSize(size);

        Page<TokenRecord> pageParam = new Page<>(pageNo, pageSize);

        LambdaQueryWrapper<TokenRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TokenRecord::getDeleted, 0);

        String queryStatus = normalizeStatus(status);

        if (!"all".equals(queryStatus)) {
            wrapper.eq(TokenRecord::getStatus, queryStatus);
        }

        if (StringUtils.hasText(search)) {
            String keyword = search.trim();

            wrapper.and(w -> w
                    .like(TokenRecord::getId, keyword)
                    .or()
                    .like(TokenRecord::getAsset, keyword)
                    .or()
                    .like(TokenRecord::getOwner, keyword)
            );
        }

        wrapper.orderByDesc(TokenRecord::getIssueDate);

        Page<TokenRecord> resultPage = tokenRecordMapper.selectPage(pageParam, wrapper);

        List<TokenListResponse> items = resultPage.getRecords()
                .stream()
                .map(this::toTokenListResponse)
                .toList();

        TokenPageResponse response = new TokenPageResponse();
        response.setItems(items);
        response.setTotal(resultPage.getTotal());
        response.setPage(pageNo);
        response.setSize(pageSize);

        return response;
    }

    /**
     * POST /api/tokens
     *
     * 发行 Token。
     *
     * 前端只传：
     * crop_type
     * batch_id
     * quantity_kg
     *
     * 后端通过 batch_id 查询 farm_batch，
     * 再通过 farm_id 查询 farm，
     * 自动得到 farmId 和 owner。
     */
    @Transactional
    public TokenIssueResponse issue(TokenIssueRequest request) {
        LocalDateTime now = LocalDateTime.now();

        FarmBatch batch = farmBatchMapper.selectById(request.getBatchId());

        if (batch == null || Integer.valueOf(1).equals(batch.getDeleted())) {
            throw new IllegalArgumentException("Batch not found: " + request.getBatchId());
        }

        Farm farm = farmMapper.selectById(batch.getFarmId());

        if (farm == null || Integer.valueOf(1).equals(farm.getDeleted())) {
            throw new IllegalArgumentException("Farm not found for batch: " + request.getBatchId());
        }

        TokenRecord existingToken = tokenRecordMapper.selectOne(
                new LambdaQueryWrapper<TokenRecord>()
                        .eq(TokenRecord::getDeleted, 0)
                        .eq(TokenRecord::getBatchId, request.getBatchId())
                        .last("LIMIT 1")
        );

        if (existingToken != null) {
            throw new IllegalArgumentException("Token already issued for batch: " + request.getBatchId());
        }

        String cropType = resolveCropType(request, batch);

        BigDecimal quantityKg = request.getQuantityKg();

        if (quantityKg == null || quantityKg.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("quantity_kg must be greater than 0");
        }

        if (batch.getYieldKg() != null && quantityKg.compareTo(batch.getYieldKg()) > 0) {
            throw new IllegalArgumentException("quantity_kg cannot be greater than batch yield_kg");
        }

        String tokenId = generateTokenId();
        String txHash = generateTxHash();

        String ownerName = resolveOwnerName(farm);
        String ownerAddress = generateOwnerAddress(farm.getId(), ownerName);

        TokenRecord token = new TokenRecord();
        token.setId(tokenId);
        token.setBatchId(batch.getId());
        token.setFarmId(farm.getId());
        token.setCropType(cropType);
        token.setAsset(buildAssetName(cropType, batch.getId()));
        token.setQuantityKg(quantityKg);
        token.setTokenAmount(quantityKg);
        token.setOwner(ownerName);
        token.setOwnerAddress(ownerAddress);
        token.setStatus("normal");
        token.setTxHash(txHash);
        token.setIssueDate(now);
        token.setDeleted(0);

        tokenRecordMapper.insert(token);

        TransactionRecord transaction = new TransactionRecord();
        transaction.setId(generateTransactionId());
        transaction.setTokenId(tokenId);
        transaction.setFromParty("System");
        transaction.setToParty(ownerName);
        transaction.setTxType("issue");
        transaction.setTxHash(txHash);
        transaction.setTxDate(now);
        transaction.setStatus("completed");
        transaction.setDeleted(0);

        transactionRecordMapper.insert(transaction);

        TokenIssueResponse response = new TokenIssueResponse();
        response.setTokenId(tokenId);
        response.setTxHash(txHash);

        return response;
    }

    /**
     * POST /api/tokens/{tokenId}/transfer
     *
     * 转移 Token。
     */
    @Transactional
    public TokenTransferResponse transfer(
            String tokenId,
            TokenTransferRequest request
    ) {
        TokenRecord token = tokenRecordMapper.selectById(tokenId);

        if (token == null || Integer.valueOf(1).equals(token.getDeleted())) {
            throw new IllegalArgumentException("Token not found: " + tokenId);
        }

        if (!StringUtils.hasText(request.getNewOwnerAddress())) {
            throw new IllegalArgumentException("new_owner_address is required");
        }

        LocalDateTime now = LocalDateTime.now();
        String txHash = generateTxHash();

        String oldOwner = token.getOwner();
        String oldOwnerAddress = token.getOwnerAddress();

        String newOwnerAddress = request.getNewOwnerAddress().trim();
        String newOwner = newOwnerAddress;

        token.setOwner(newOwner);
        token.setOwnerAddress(newOwnerAddress);
        tokenRecordMapper.updateById(token);

        TokenTransferRecord transferRecord = new TokenTransferRecord();
        transferRecord.setTokenId(tokenId);
        transferRecord.setOldOwner(oldOwner);
        transferRecord.setOldOwnerAddress(oldOwnerAddress);
        transferRecord.setNewOwner(newOwner);
        transferRecord.setNewOwnerAddress(newOwnerAddress);
        transferRecord.setTxHash(txHash);
        transferRecord.setTransferredAt(now);
        transferRecord.setDeleted(0);

        tokenTransferRecordMapper.insert(transferRecord);

        TransactionRecord transaction = new TransactionRecord();
        transaction.setId(generateTransactionId());
        transaction.setTokenId(tokenId);
        transaction.setFromParty(oldOwner);
        transaction.setToParty(newOwner);
        transaction.setTxType("transfer");
        transaction.setTxHash(txHash);
        transaction.setTxDate(now);
        transaction.setStatus("completed");
        transaction.setDeleted(0);

        transactionRecordMapper.insert(transaction);

        TokenTransferResponse response = new TokenTransferResponse();
        response.setTxHash(txHash);
        response.setTransferredAt(toIsoUtc(now));

        return response;
    }

    /**
     * GET /api/tokens/transfers
     *
     * 查询全部或某个 farmId 的转账记录。
     */
    public List<TokenTransferRecordResponse> listTransferRecords(String farmId) {
        LambdaQueryWrapper<TokenTransferRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TokenTransferRecord::getDeleted, 0);

        if (StringUtils.hasText(farmId)) {
            List<TokenRecord> tokenRecords = tokenRecordMapper.selectList(
                    new LambdaQueryWrapper<TokenRecord>()
                            .eq(TokenRecord::getDeleted, 0)
                            .eq(TokenRecord::getFarmId, farmId.trim())
            );

            List<String> tokenIds = tokenRecords.stream()
                    .map(TokenRecord::getId)
                    .toList();

            if (tokenIds.isEmpty()) {
                return List.of();
            }

            wrapper.in(TokenTransferRecord::getTokenId, tokenIds);
        }

        wrapper.orderByDesc(TokenTransferRecord::getTransferredAt);

        return tokenTransferRecordMapper.selectList(wrapper)
                .stream()
                .map(this::toTokenTransferRecordResponse)
                .toList();
    }

    /**
     * GET /api/tokens/{tokenId}/transfers
     *
     * 查询某个 Token 的转账历史。
     */
    public List<TokenTransferHistoryResponse> listTransferHistory(String tokenId) {
        LambdaQueryWrapper<TokenTransferRecord> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(TokenTransferRecord::getDeleted, 0)
                .eq(TokenTransferRecord::getTokenId, tokenId)
                .orderByDesc(TokenTransferRecord::getTransferredAt);

        return tokenTransferRecordMapper.selectList(wrapper)
                .stream()
                .map(this::toTokenTransferHistoryResponse)
                .toList();
    }

    private TokenListResponse toTokenListResponse(TokenRecord record) {
        TokenListResponse response = new TokenListResponse();

        response.setId(record.getId());
        response.setAsset(record.getAsset());
        response.setOwner(record.getOwner());
        response.setStatus(record.getStatus());
        response.setDate(toIsoUtc(record.getIssueDate()));

        return response;
    }

    private TokenTransferRecordResponse toTokenTransferRecordResponse(TokenTransferRecord record) {
        TokenTransferRecordResponse response = new TokenTransferRecordResponse();

        response.setId(record.getId());
        response.setTokenId(record.getTokenId());
        response.setOldOwner(record.getOldOwner());
        response.setOldOwnerAddress(record.getOldOwnerAddress());
        response.setNewOwner(record.getNewOwner());
        response.setNewOwnerAddress(record.getNewOwnerAddress());
        response.setTxHash(record.getTxHash());
        response.setTransferredAt(toIsoUtc(record.getTransferredAt()));

        return response;
    }

    private TokenTransferHistoryResponse toTokenTransferHistoryResponse(TokenTransferRecord record) {
        TokenTransferHistoryResponse response = new TokenTransferHistoryResponse();

        response.setId(record.getId());
        response.setTokenId(record.getTokenId());
        response.setOldOwner(record.getOldOwner());
        response.setOldOwnerAddress(record.getOldOwnerAddress());
        response.setNewOwner(record.getNewOwner());
        response.setNewOwnerAddress(record.getNewOwnerAddress());
        response.setTxHash(record.getTxHash());
        response.setTransferredAt(toIsoUtc(record.getTransferredAt()));

        return response;
    }

    private String resolveCropType(TokenIssueRequest request, FarmBatch batch) {
        if (StringUtils.hasText(request.getCropType())) {
            return request.getCropType().trim();
        }

        if (StringUtils.hasText(batch.getCropType())) {
            return batch.getCropType().trim();
        }

        throw new IllegalArgumentException("crop_type is required");
    }

    private String resolveOwnerName(Farm farm) {
        if (StringUtils.hasText(farm.getFarmName())) {
            return farm.getFarmName().trim();
        }

        if (StringUtils.hasText(farm.getOwnerName())) {
            return farm.getOwnerName().trim();
        }

        return farm.getId();
    }

    private int normalizePage(Integer page) {
        if (page == null || page < 1) {
            return 1;
        }

        return page;
    }

    private int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return 20;
        }

        return Math.min(size, 100);
    }

    private String normalizeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return "all";
        }

        String value = status.trim();

        if ("all".equals(value)
                || "normal".equals(value)
                || "flagged".equals(value)
                || "at-risk".equals(value)) {
            return value;
        }

        return "all";
    }

    private String buildAssetName(String cropType, String batchId) {
        String crop = StringUtils.hasText(cropType) ? cropType.trim() : "Crop";
        String batch = StringUtils.hasText(batchId) ? batchId.trim() : "Batch";

        return crop + " " + batch;
    }

    private String toIsoUtc(LocalDateTime time) {
        if (time == null) {
            return null;
        }

        return time
                .atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT);
    }

    private String generateTokenId() {
        return "TKN-" + System.currentTimeMillis() + "-" + SECURE_RANDOM.nextInt(1000);
    }

    private String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis() + "-" + SECURE_RANDOM.nextInt(1000);
    }

    private String generateTxHash() {
        StringBuilder sb = new StringBuilder("0x");

        for (int i = 0; i < 64; i++) {
            sb.append(Integer.toHexString(SECURE_RANDOM.nextInt(16)));
        }

        return sb.toString();
    }

    private String generateOwnerAddress(String farmId, String ownerName) {
        String seed = farmId + "-" + ownerName + "-" + System.nanoTime();

        StringBuilder sb = new StringBuilder("0x");

        int hash = seed.hashCode();

        for (int i = 0; i < 40; i++) {
            int value = Math.abs(hash + i * 31 + SECURE_RANDOM.nextInt(16)) % 16;
            sb.append(Integer.toHexString(value));
        }

        return sb.toString();
    }
}