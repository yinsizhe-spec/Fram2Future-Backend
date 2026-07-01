package com.farm2future.farm2future_backend.model.fram.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.farm2future.farm2future_backend.model.fram.dto.TokenIssueRequest;
import com.farm2future.farm2future_backend.model.fram.dto.TokenIssueResponse;
import com.farm2future.farm2future_backend.model.fram.entity.Farm;
import com.farm2future.farm2future_backend.model.fram.entity.FarmBatch;
import com.farm2future.farm2future_backend.model.fram.entity.TokenRecord;
import com.farm2future.farm2future_backend.model.fram.entity.TransactionRecord;
import com.farm2future.farm2future_backend.model.fram.mapper.FarmBatchMapper;
import com.farm2future.farm2future_backend.model.fram.mapper.FarmMapper;
import com.farm2future.farm2future_backend.model.fram.mapper.TokenRecordMapper;
import com.farm2future.farm2future_backend.model.fram.mapper.TransactionRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRecordMapper tokenRecordMapper;
    private final TransactionRecordMapper transactionRecordMapper;
    private final FarmBatchMapper farmBatchMapper;
    private final FarmMapper farmMapper;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 发行 Token
     *
     * 对应接口：
     * POST /api/tokens
     */
    @Transactional
    public TokenIssueResponse issue(TokenIssueRequest request) {
        // 1. 校验批次是否存在
        FarmBatch batch = farmBatchMapper.selectOne(
                new LambdaQueryWrapper<FarmBatch>()
                        .eq(FarmBatch::getId, request.getBatchId())
                        .eq(FarmBatch::getDeleted, 0)
        );

        if (batch == null) {
            throw new RuntimeException("Batch not found: " + request.getBatchId());
        }

        // 2. 校验 crop_type 是否和批次一致
        if (batch.getCropType() != null
                && !batch.getCropType().equalsIgnoreCase(request.getCropType())) {
            throw new RuntimeException("crop_type does not match the batch crop type");
        }

        // 3. 校验发行数量不能超过批次产量
        if (batch.getYieldKg() != null
                && request.getQuantityKg().compareTo(batch.getYieldKg()) > 0) {
            throw new RuntimeException("quantity_kg cannot be greater than batch yield_kg");
        }

        // 4. 查询农场信息，用 farm_name 作为 owner
        Farm farm = farmMapper.selectOne(
                new LambdaQueryWrapper<Farm>()
                        .eq(Farm::getId, batch.getFarmId())
                        .eq(Farm::getDeleted, 0)
        );

        String ownerName = farm != null ? farm.getFarmName() : "Unknown Farm";

        // 5. 生成 token id 和模拟区块链交易 hash
        String tokenId = generateTokenId();
        String txHash = generateTxHash();

        LocalDateTime now = LocalDateTime.now();

        // 6. 保存 token_record
        TokenRecord tokenRecord = new TokenRecord();
        tokenRecord.setId(tokenId);
        tokenRecord.setBatchId(batch.getId());
        tokenRecord.setFarmId(batch.getFarmId());
        tokenRecord.setCropType(request.getCropType());
        tokenRecord.setAsset(request.getCropType() + " " + batch.getId());
        tokenRecord.setQuantityKg(request.getQuantityKg());

        // token_amount 兼容旧统计字段，这里和 quantity_kg 保持一致
        tokenRecord.setTokenAmount(request.getQuantityKg());

        tokenRecord.setOwner(ownerName);
        tokenRecord.setOwnerAddress(null);
        tokenRecord.setStatus("normal");
        tokenRecord.setTxHash(txHash);
        tokenRecord.setIssueDate(now);
        tokenRecord.setDeleted(0);
        tokenRecord.setCreateTime(now);
        tokenRecord.setUpdateTime(now);

        tokenRecordMapper.insert(tokenRecord);

        // 7. 保存 transaction_record
        TransactionRecord transactionRecord = new TransactionRecord();
        transactionRecord.setId(generateTransactionId());
        transactionRecord.setTokenId(tokenId);
        transactionRecord.setFromParty("System");
        transactionRecord.setToParty(ownerName);
        transactionRecord.setTxType("issue");
        transactionRecord.setTxHash(txHash);
        transactionRecord.setTxDate(now);
        transactionRecord.setStatus("completed");
        transactionRecord.setDeleted(0);
        transactionRecord.setCreateTime(now);
        transactionRecord.setUpdateTime(now);

        transactionRecordMapper.insert(transactionRecord);

        // 8. 返回给前端
        return new TokenIssueResponse(tokenId, txHash);
    }

    private String generateTokenId() {
        int year = Year.now().getValue();

        long count = tokenRecordMapper.selectCount(
                new LambdaQueryWrapper<TokenRecord>()
                        .likeRight(TokenRecord::getId, "TKN-" + year + "-")
        );

        return String.format("TKN-%d-%03d", year, count + 1);
    }

    private String generateTransactionId() {
        int year = Year.now().getValue();

        long count = transactionRecordMapper.selectCount(
                new LambdaQueryWrapper<TransactionRecord>()
                        .likeRight(TransactionRecord::getId, "TXN-" + year + "-")
        );

        return String.format("TXN-%d-%03d", year, count + 1);
    }

    /**
     * 生成模拟区块链交易 Hash
     * 格式：0x + 64位十六进制
     */
    private String generateTxHash() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return "0x" + HexFormat.of().formatHex(bytes);
    }
}