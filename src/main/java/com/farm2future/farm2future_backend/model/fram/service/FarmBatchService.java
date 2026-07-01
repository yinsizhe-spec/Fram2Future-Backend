package com.farm2future.farm2future_backend.model.fram.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.farm2future.farm2future_backend.model.fram.dto.FarmBatchOptionResponse;
import com.farm2future.farm2future_backend.model.fram.entity.Farm;
import com.farm2future.farm2future_backend.model.fram.entity.FarmBatch;
import com.farm2future.farm2future_backend.model.fram.mapper.FarmBatchMapper;
import com.farm2future.farm2future_backend.model.fram.mapper.FarmMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FarmBatchService {

    private final FarmBatchMapper farmBatchMapper;
    private final FarmMapper farmMapper;

    /**
     * 查询可用于发行 Token 的 batch_id 列表
     */
    public List<FarmBatchOptionResponse> listBatchOptions(String farmId, String cropType) {
        LambdaQueryWrapper<FarmBatch> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(FarmBatch::getDeleted, 0);

        if (farmId != null && !farmId.isBlank()) {
            wrapper.eq(FarmBatch::getFarmId, farmId);
        }

        if (cropType != null && !cropType.isBlank()) {
            wrapper.eq(FarmBatch::getCropType, cropType);
        }

        wrapper.orderByDesc(FarmBatch::getBatchDate);

        List<FarmBatch> batches = farmBatchMapper.selectList(wrapper);

        return batches.stream().map(batch -> {
            FarmBatchOptionResponse response = new FarmBatchOptionResponse();

            response.setBatchId(batch.getId());
            response.setFarmId(batch.getFarmId());
            response.setCropType(batch.getCropType());
            response.setBatchDate(batch.getBatchDate());
            response.setYieldKg(batch.getYieldKg());

            // 目前先把可发行数量等于 yield_kg
            // 后面如果要扣除已经发行过的 Token，可以再改这里
            response.setAvailableQuantityKg(
                    batch.getYieldKg() == null ? BigDecimal.ZERO : batch.getYieldKg()
            );

            Farm farm = farmMapper.selectOne(
                    new LambdaQueryWrapper<Farm>()
                            .eq(Farm::getId, batch.getFarmId())
                            .eq(Farm::getDeleted, 0)
            );

            response.setFarmName(farm == null ? null : farm.getFarmName());

            return response;
        }).toList();
    }
}