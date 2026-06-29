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

/**
 * 农场数据提交业务服务类
 *
 * <p>
 * 该 Service 主要负责处理：
 * POST /api/farms/{farmId}/data
 * 接口中的核心业务逻辑。
 * </p>
 *
 * <p>
 * 主要功能包括：
 * 1. 校验 URL 中的 farmId 是否和请求体中的 farm_id 一致
 * 2. 校验农场是否存在
 * 3. 校验销售数量是否超过产量
 * 4. 保存农作物批次数据到 farm_batch 表
 * 5. 保存 IoT 环境快照数据到 iot_snapshot 表
 * 6. 生成模拟区块链交易哈希并保存交易记录
 * 7. 返回批次 ID、交易哈希和提交时间
 * </p>
 */
@Service
@RequiredArgsConstructor
public class FarmDataService {

    /**
     * 农场表 Mapper
     *
     * <p>
     * 用于查询 farm 表，判断农场是否存在。
     * </p>
     */
    private final FarmMapper farmMapper;

    /**
     * 农场批次数据 Mapper
     *
     * <p>
     * 用于向 farm_batch 表插入农作物批次数据。
     * </p>
     */
    private final FarmBatchMapper farmBatchMapper;

    /**
     * IoT 快照数据 Mapper
     *
     * <p>
     * 用于向 iot_snapshot 表插入传感器环境数据。
     * </p>
     */
    private final IotSnapshotMapper iotSnapshotMapper;

    /**
     * 交易记录 Mapper
     *
     * <p>
     * 用于向 transaction_record 表插入模拟区块链交易记录。
     * </p>
     */
    private final TransactionRecordMapper transactionRecordMapper;

    /**
     * 提交农场数据
     *
     * <p>
     * 该方法会同时写入 farm_batch、iot_snapshot 和 transaction_record 三张表。
     * 因为方法上添加了 @Transactional，所以只要其中任意一步插入失败，
     * 整个数据库操作都会回滚，避免出现部分数据保存成功、部分失败的情况。
     * </p>
     *
     * @param farmIdFromPath URL 路径中的农场 ID，例如 /api/farms/farm_1/data 中的 farm_1
     * @param request        前端提交的农场数据请求体
     * @return FarmDataSubmitResponse 返回批次 ID、交易哈希和提交时间
     */
    @Transactional
    public FarmDataSubmitResponse submitFarmData(String farmIdFromPath, FarmDataSubmitRequest request) {

        /*
         * 校验路径中的 farmId 是否和请求体中的 farm_id 一致。
         *
         * 这样做是为了避免前端 URL 里传的是 farm_1，
         * 但请求体里传的是 farm_2，导致数据被错误地保存到其他农场。
         */
        if (!farmIdFromPath.equals(request.getFarmId())) {
            throw new RuntimeException("farmId in URL does not match farm_id in request body");
        }

        /*
         * 根据 farmId 查询农场信息。
         *
         * 查询条件：
         * 1. id = farmIdFromPath
         * 2. deleted = 0，表示只查询未被逻辑删除的农场
         * 3. LIMIT 1，限制只查询一条记录
         */
        Farm farm = farmMapper.selectOne(
                new LambdaQueryWrapper<Farm>()
                        .eq(Farm::getId, farmIdFromPath)
                        .eq(Farm::getDeleted, 0)
                        .last("LIMIT 1")
        );

        /*
         * 如果数据库中找不到对应农场，则说明前端提交的 farmId 无效。
         */
        if (farm == null) {
            throw new RuntimeException("Farm not found: " + farmIdFromPath);
        }

        /*
         * 获取请求体中的 batch 数据。
         *
         * batch 中包含：
         * 作物类型、日期、产量、用水量、肥料使用量、销售数量、销售单价、成本等。
         */
        FarmDataSubmitRequest.Batch batchRequest = request.getBatch();

        /*
         * 业务校验：
         * 销售数量不能大于总产量。
         *
         * 例如：
         * yield_kg = 100
         * sale_quantity_kg = 120
         * 这种情况是不合理的，所以需要阻止提交。
         */
        if (batchRequest.getSaleQuantityKg().compareTo(batchRequest.getYieldKg()) > 0) {
            throw new RuntimeException("sale_quantity_kg cannot be greater than yield_kg");
        }

        /*
         * 获取当前系统时间。
         *
         * now 用于保存到数据库中的 submitted_at 和 tx_date 字段。
         */
        LocalDateTime now = LocalDateTime.now();

        /*
         * 将提交时间转换为 UTC ISO 格式字符串，用于返回给前端。
         *
         * 示例：
         * 2026-06-29T10:30:00Z
         */
        String submittedAtUtc = now.atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT);

        /*
         * 生成批次 ID。
         *
         * 示例：
         * BCH-1A2B3C4D
         */
        String batchId = generateBatchId();

        /*
         * 生成交易记录 ID。
         *
         * 示例：
         * TXN-1A2B3C4D
         */
        String txId = generateTxId();

        /*
         * 生成模拟区块链交易哈希。
         *
         * 注意：
         * 当前这里不是真正调用智能合约，
         * 而是用 SHA-256 生成一个类似区块链交易哈希的字符串。
         */
        String txHash = generateTxHash(farmIdFromPath, batchId, now);

        /*
         * 创建 FarmBatch 实体对象。
         *
         * 该对象最终会被插入到 farm_batch 表中，
         * 用于保存本次农场提交的农作物批次数据。
         */
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

        /*
         * 将农作物批次数据插入 farm_batch 表。
         */
        farmBatchMapper.insert(batch);

        /*
         * 获取请求体中的 iot_snapshot 数据。
         *
         * iot_snapshot 中包含：
         * 土壤湿度、温度、空气湿度、PH 值等传感器数据。
         */
        FarmDataSubmitRequest.IotSnapshot snapshotRequest = request.getIotSnapshot();

        /*
         * 创建 IotSnapshot 实体对象。
         *
         * 该对象最终会被插入到 iot_snapshot 表中。
         */
        IotSnapshot snapshot = new IotSnapshot();
        snapshot.setBatchId(batchId);
        snapshot.setFarmId(farmIdFromPath);
        snapshot.setSoilMoisturePct(snapshotRequest.getSoilMoisturePct());
        snapshot.setTemperatureC(snapshotRequest.getTemperatureC());
        snapshot.setHumidityPct(snapshotRequest.getHumidityPct());
        snapshot.setPhLevel(snapshotRequest.getPhLevel());
        snapshot.setDeleted(0);

        /*
         * 将 IoT 传感器快照数据插入 iot_snapshot 表。
         */
        iotSnapshotMapper.insert(snapshot);

        /*
         * 创建交易记录对象。
         *
         * 当前项目这里相当于模拟“农场数据上链记录”。
         * 后续如果真正接入 Solidity 智能合约，
         * txHash 可以替换为真实区块链交易哈希。
         */
        TransactionRecord transactionRecord = new TransactionRecord();
        transactionRecord.setId(txId);

        /*
         * 当前提交的是农场数据，不是具体 Token 交易，
         * 所以 tokenId 暂时设置为 null。
         */
        transactionRecord.setTokenId(null);

        /*
         * 交易发起方。
         *
         * 这里使用农场名称作为 fromParty。
         */
        transactionRecord.setFromParty(farm.getFarmName());

        /*
         * 交易接收方。
         *
         * 当前为模拟区块链账本名称。
         */
        transactionRecord.setToParty("Farm2Future Blockchain Ledger");

        /*
         * 交易类型。
         *
         * farm_data_anchor 表示“农场数据锚定到区块链账本”。
         */
        transactionRecord.setTxType("farm_data_anchor");

        /*
         * 保存模拟交易哈希。
         */
        transactionRecord.setTxHash(txHash);

        /*
         * 保存交易发生时间。
         */
        transactionRecord.setTxDate(now);

        /*
         * 保存交易状态。
         *
         * completed 表示当前模拟交易已完成。
         */
        transactionRecord.setStatus("completed");

        /*
         * 设置逻辑删除标记。
         *
         * 0 表示未删除。
         */
        transactionRecord.setDeleted(0);

        /*
         * 将交易记录插入 transaction_record 表。
         */
        transactionRecordMapper.insert(transactionRecord);

        /*
         * 返回给前端的数据。
         *
         * 前端可以拿到：
         * 1. batch_id：本次提交的批次 ID
         * 2. tx_hash：模拟区块链交易哈希
         * 3. submitted_at：提交时间
         */
        return new FarmDataSubmitResponse(
                batchId,
                txHash,
                submittedAtUtc
        );
    }

    /**
     * 生成农场批次 ID
     *
     * <p>
     * 使用 UUID 生成随机字符串，
     * 去掉横线后截取前 8 位，并转换为大写。
     * </p>
     *
     * <p>
     * 返回示例：
     * BCH-1A2B3C4D
     * </p>
     *
     * @return 批次 ID
     */
    private String generateBatchId() {
        String random = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();

        return "BCH-" + random;
    }

    /**
     * 生成交易记录 ID
     *
     * <p>
     * 使用 UUID 生成随机字符串，
     * 去掉横线后截取前 8 位，并转换为大写。
     * </p>
     *
     * <p>
     * 返回示例：
     * TXN-1A2B3C4D
     * </p>
     *
     * @return 交易记录 ID
     */
    private String generateTxId() {
        String random = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();

        return "TXN-" + random;
    }

    /**
     * 生成模拟区块链交易哈希
     *
     * <p>
     * 当前方法并没有真正调用区块链或智能合约。
     * 它只是把 farmId、batchId、当前时间和随机 UUID 拼接后，
     * 使用 SHA-256 算法生成一个 64 位十六进制字符串，
     * 再在前面加上 0x，使其看起来类似区块链交易哈希。
     * </p>
     *
     * <p>
     * 后续如果接入真实区块链，例如 Solidity 智能合约，
     * 可以将该方法替换为真实的合约调用逻辑。
     * </p>
     *
     * @param farmId  农场 ID
     * @param batchId 批次 ID
     * @param now     当前提交时间
     * @return 模拟交易哈希
     */
    private String generateTxHash(String farmId, String batchId, LocalDateTime now) {
        try {
            /*
             * 拼接原始字符串。
             *
             * 加入 UUID.randomUUID() 是为了确保每次生成的哈希都不同。
             */
            String raw = farmId + "|" + batchId + "|" + now + "|" + UUID.randomUUID();

            /*
             * 获取 SHA-256 摘要算法实例。
             */
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            /*
             * 对原始字符串进行 SHA-256 哈希计算。
             */
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));

            /*
             * 将 byte[] 转换为十六进制字符串，并加上 0x 前缀。
             */
            return "0x" + HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            /*
             * 如果 SHA-256 生成失败，则使用两个 UUID 拼接作为备用哈希。
             *
             * 正常情况下这里基本不会触发。
             */
            return "0x" + UUID.randomUUID().toString().replace("-", "")
                    + UUID.randomUUID().toString().replace("-", "");
        }
    }
}