package com.farm2future.farm2future_backend.model.fram.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.farm2future.farm2future_backend.model.fram.dto.*;
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

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRecordMapper tokenRecordMapper;
    private final TransactionRecordMapper transactionRecordMapper;
    private final TokenTransferRecordMapper tokenTransferRecordMapper;
    private final FarmBatchMapper farmBatchMapper;
    private final FarmMapper farmMapper;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 发行 Token
     */
    @Transactional
    public TokenIssueResponse issue(TokenIssueRequest request) {
        LocalDateTime now = LocalDateTime.now();

        FarmBatch batch = farmBatchMapper.selectOne(
                new LambdaQueryWrapper<FarmBatch>()
                        .eq(FarmBatch::getId, request.getBatchId())
                        .eq(FarmBatch::getDeleted, 0)
        );

        if (batch == null) {
            throw new RuntimeException("Batch not found: " + request.getBatchId());
        }

        Farm farm = farmMapper.selectOne(
                new LambdaQueryWrapper<Farm>()
                        .eq(Farm::getId, batch.getFarmId())
                        .eq(Farm::getDeleted, 0)
        );

        String ownerName = farm == null ? "Unknown Farm" : farm.getFarmName();

        String tokenId = generateTokenId();
        String txHash = generateTxHash();

        TokenRecord tokenRecord = new TokenRecord();
        tokenRecord.setId(tokenId);
        tokenRecord.setBatchId(request.getBatchId());
        tokenRecord.setFarmId(batch.getFarmId());
        tokenRecord.setCropType(request.getCropType());
        tokenRecord.setAsset(request.getCropType() + " " + request.getBatchId());
        tokenRecord.setQuantityKg(request.getQuantityKg());
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

        return new TokenIssueResponse(tokenId, txHash);
    }

    /**
     * 转移 Token 所有权
     *
     * 对应接口：
     * POST /api/tokens/{tokenId}/transfer
     */
    @Transactional
    public TokenTransferResponse transfer(String tokenId, TokenTransferRequest request) {
        LocalDateTime now = LocalDateTime.now();

        // 1. 校验 URL 里的 tokenId 和请求体里的 token_id 是否一致
        if (request.getTokenId() != null && !request.getTokenId().equals(tokenId)) {
            throw new RuntimeException("token_id in request body does not match tokenId in URL");
        }

        // 2. 查询 Token 是否存在
        TokenRecord tokenRecord = tokenRecordMapper.selectOne(
                new LambdaQueryWrapper<TokenRecord>()
                        .eq(TokenRecord::getId, tokenId)
                        .eq(TokenRecord::getDeleted, 0)
        );

        if (tokenRecord == null) {
            throw new RuntimeException("Token not found: " + tokenId);
        }

        // 3. 保存旧 owner 信息
        String oldOwner = tokenRecord.getOwner();
        String oldOwnerAddress = tokenRecord.getOwnerAddress();

        // 4. 生成模拟区块链交易 hash
        String txHash = generateTxHash();

        // 5. 更新 token_record 当前 owner 信息
        // 目前接口只传 new_owner_address，没有传 new_owner 名称
        // 所以 owner 这里先保存地址，方便前端查看
        tokenRecord.setOwner(request.getNewOwnerAddress());
        tokenRecord.setOwnerAddress(request.getNewOwnerAddress());
        tokenRecord.setUpdateTime(now);

        tokenRecordMapper.updateById(tokenRecord);

        // 6. 保存 token_transfer_record 转移记录
        TokenTransferRecord transferRecord = new TokenTransferRecord();
        transferRecord.setTokenId(tokenId);
        transferRecord.setOldOwner(oldOwner);
        transferRecord.setOldOwnerAddress(oldOwnerAddress);
        transferRecord.setNewOwner(request.getNewOwnerAddress());
        transferRecord.setNewOwnerAddress(request.getNewOwnerAddress());
        transferRecord.setTxHash(txHash);
        transferRecord.setTransferredAt(now);
        transferRecord.setDeleted(0);
        transferRecord.setCreateTime(now);
        transferRecord.setUpdateTime(now);

        tokenTransferRecordMapper.insert(transferRecord);

        // 7. 保存 transaction_record 审计交易记录
        TransactionRecord transactionRecord = new TransactionRecord();
        transactionRecord.setId(generateTransactionId());
        transactionRecord.setTokenId(tokenId);
        transactionRecord.setFromParty(oldOwner == null ? "Unknown" : oldOwner);
        transactionRecord.setToParty(request.getNewOwnerAddress());
        transactionRecord.setTxType("transfer");
        transactionRecord.setTxHash(txHash);
        transactionRecord.setTxDate(now);
        transactionRecord.setStatus("completed");
        transactionRecord.setDeleted(0);
        transactionRecord.setCreateTime(now);
        transactionRecord.setUpdateTime(now);

        transactionRecordMapper.insert(transactionRecord);

        return new TokenTransferResponse(txHash, now);
    }

    public List<TokenListResponse> listTokens(String status, String owner, String cropType) {
        LambdaQueryWrapper<TokenRecord> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(TokenRecord::getDeleted, 0);

        if (status != null && !status.isBlank()) {
            wrapper.eq(TokenRecord::getStatus, status);
        }

        if (owner != null && !owner.isBlank()) {
            wrapper.like(TokenRecord::getOwner, owner);
        }

        if (cropType != null && !cropType.isBlank()) {
            wrapper.eq(TokenRecord::getCropType, cropType);
        }

        wrapper.orderByDesc(TokenRecord::getIssueDate);

        List<TokenRecord> records = tokenRecordMapper.selectList(wrapper);

        return records.stream().map(record -> {
            TokenListResponse response = new TokenListResponse();

            response.setTokenId(record.getId());
            response.setBatchId(record.getBatchId());
            response.setFarmId(record.getFarmId());
            response.setCropType(record.getCropType());
            response.setAsset(record.getAsset());
            response.setQuantityKg(record.getQuantityKg());
            response.setTokenAmount(record.getTokenAmount());
            response.setOwner(record.getOwner());
            response.setOwnerAddress(record.getOwnerAddress());
            response.setStatus(record.getStatus());
            response.setTxHash(record.getTxHash());
            response.setIssueDate(record.getIssueDate());

            return response;
        }).toList();
    }

    public List<TokenTransferHistoryResponse> listTransferHistory(String tokenId) {
        TokenRecord tokenRecord = tokenRecordMapper.selectOne(
                new LambdaQueryWrapper<TokenRecord>()
                        .eq(TokenRecord::getId, tokenId)
                        .eq(TokenRecord::getDeleted, 0)
        );

        if (tokenRecord == null) {
            throw new RuntimeException("Token not found: " + tokenId);
        }

        List<TokenTransferRecord> records = tokenTransferRecordMapper.selectList(
                new LambdaQueryWrapper<TokenTransferRecord>()
                        .eq(TokenTransferRecord::getTokenId, tokenId)
                        .eq(TokenTransferRecord::getDeleted, 0)
                        .orderByDesc(TokenTransferRecord::getTransferredAt)
        );

        return records.stream().map(record -> {
            TokenTransferHistoryResponse response = new TokenTransferHistoryResponse();

            response.setId(record.getId());
            response.setTokenId(record.getTokenId());
            response.setOldOwner(record.getOldOwner());
            response.setOldOwnerAddress(record.getOldOwnerAddress());
            response.setNewOwner(record.getNewOwner());
            response.setNewOwnerAddress(record.getNewOwnerAddress());
            response.setTxHash(record.getTxHash());
            response.setTransferredAt(record.getTransferredAt());

            return response;
        }).toList();
    }

    private String generateTokenId() {
        int year = Year.now().getValue();

        Long count = tokenRecordMapper.selectCount(
                new LambdaQueryWrapper<TokenRecord>()
                        .likeRight(TokenRecord::getId, "TKN-" + year + "-")
        );

        return String.format("TKN-%d-%03d", year, count + 1);
    }

    private String generateTransactionId() {
        int year = Year.now().getValue();

        Long count = transactionRecordMapper.selectCount(
                new LambdaQueryWrapper<TransactionRecord>()
                        .likeRight(TransactionRecord::getId, "TXN-" + year + "-")
        );

        return String.format("TXN-%d-%03d", year, count + 1);
    }

    private String generateTxHash() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return "0x" + HexFormat.of().formatHex(bytes);
    }
}