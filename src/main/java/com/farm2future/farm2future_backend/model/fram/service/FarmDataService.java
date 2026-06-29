package com.farm2future.farm2future_backend.model.fram.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.farm2future.farm2future_backend.model.fram.dto.FarmDataSubmitRequest;
import com.farm2future.farm2future_backend.model.fram.dto.FarmDataSubmitResponse;
import com.farm2future.farm2future_backend.model.fram.entity.Farm;
import com.farm2future.farm2future_backend.model.fram.entity.FarmBatch;
import com.farm2future.farm2future_backend.model.fram.entity.IotSnapshot;
import com.farm2future.farm2future_backend.model.fram.entity.TransactionRecord;
import com.farm2future.farm2future_backend.model.fram.mapper.FarmBatchMapper;
import com.farm2future.farm2future_backend.model.fram.mapper.FarmMapper;
import com.farm2future.farm2future_backend.model.fram.mapper.IotSnapshotMapper;
import com.farm2future.farm2future_backend.model.fram.mapper.TransactionRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FarmDataService {
    private final FarmMapper farmMapper;
    private final FarmBatchMapper farmBatchMapper;
    private final IotSnapshotMapper iotSnapshotMapper;
    private final TransactionRecordMapper transactionRecordMapper;

    @Transactional
    public FarmDataSubmitResponse submitFarmData(String farmIdFromPath, FarmDataSubmitRequest request) {
        if (!farmIdFromPath.equals(request.getFarmId())) {
            throw new RuntimeException("farmId in URL does not match farm_id in request body");
        }

        Farm farm = farmMapper.selectOne(
                new LambdaQueryWrapper<Farm>()
                        .eq(Farm::getId, farmIdFromPath)
                        .eq(Farm::getDeleted, 0)
                        .last("LIMIT 1")
        );

        if (farm == null) {
            throw new RuntimeException("Farm not found: " + farmIdFromPath);
        }

        FarmDataSubmitRequest.Batch batchRequest = request.getBatch();

        if (batchRequest.getSaleQuantityKg().compareTo(batchRequest.getYieldKg()) > 0) {
            throw new RuntimeException("sale_quantity_kg cannot be greater than yield_kg");
        }

        LocalDateTime now = LocalDateTime.now();
        String submittedAtUtc = now.atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT);

        String batchId = generateBatchId();
        String txId = generateTxId();
        String txHash = generateTxHash(farmIdFromPath, batchId, now);

        FarmBatch batch = new FarmBatch();
        batch.setId(batchId);
        batch.setFarmId(farmIdFromPath);
        batch.setCropType(batchRequest.getCropType());
        batch.setBatchDate(batchRequest.getDate());
        batch.setYieldKg(batchRequest.getYieldKg());
        batch.setWaterUsageL(batchRequest.getWaterUsageL());
        batch.setFertiliserType(batchRequest.getFertiliserType());
        batch.setFertiliserUsageKg(batchRequest.getFertiliserUsageKg());
        batch.setSaleQuantityKg(batchRequest.getSaleQuantityKg());
        batch.setSaleUnitPriceRm(batchRequest.getSaleUnitPriceRm());
        batch.setBuyerName(batchRequest.getBuyerName());
        batch.setSeedCostRm(batchRequest.getSeedCostRm());
        batch.setFertiliserCostRm(batchRequest.getFertiliserCostRm());
        batch.setTxHash(txHash);
        batch.setSubmittedAt(now);
        batch.setDeleted(0);

        farmBatchMapper.insert(batch);

        FarmDataSubmitRequest.IotSnapshot snapshotRequest = request.getIotSnapshot();

        IotSnapshot snapshot = new IotSnapshot();
        snapshot.setBatchId(batchId);
        snapshot.setFarmId(farmIdFromPath);
        snapshot.setSoilMoisturePct(snapshotRequest.getSoilMoisturePct());
        snapshot.setTemperatureC(snapshotRequest.getTemperatureC());
        snapshot.setHumidityPct(snapshotRequest.getHumidityPct());
        snapshot.setPhLevel(snapshotRequest.getPhLevel());
        snapshot.setDeleted(0);

        iotSnapshotMapper.insert(snapshot);

        TransactionRecord transactionRecord = new TransactionRecord();
        transactionRecord.setId(txId);
        transactionRecord.setTokenId(null);
        transactionRecord.setFromParty(farm.getFarmName());
        transactionRecord.setToParty("Farm2Future Blockchain Ledger");
        transactionRecord.setTxType("farm_data_anchor");
        transactionRecord.setTxHash(txHash);
        transactionRecord.setTxDate(now);
        transactionRecord.setStatus("completed");
        transactionRecord.setDeleted(0);

        transactionRecordMapper.insert(transactionRecord);

        return new FarmDataSubmitResponse(
                batchId,
                txHash,
                submittedAtUtc
        );
    }

    private String generateBatchId() {
        String random = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();

        return "BCH-" + random;
    }

    private String generateTxId() {
        String random = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();

        return "TXN-" + random;
    }

    private String generateTxHash(String farmId, String batchId, LocalDateTime now) {
        try {
            String raw = farmId + "|" + batchId + "|" + now + "|" + UUID.randomUUID();

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));

            return "0x" + HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return "0x" + UUID.randomUUID().toString().replace("-", "")
                    + UUID.randomUUID().toString().replace("-", "");
        }
    }
}
