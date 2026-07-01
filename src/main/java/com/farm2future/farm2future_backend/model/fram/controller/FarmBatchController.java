package com.farm2future.farm2future_backend.model.fram.controller;

import com.farm2future.farm2future_backend.model.fram.dto.FarmBatchOptionResponse;
import com.farm2future.farm2future_backend.model.fram.service.FarmBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/farms/batches")
@RequiredArgsConstructor
public class FarmBatchController {

    private final FarmBatchService farmBatchService;

    /**
     * 查询可用于发行 Token 的 batch_id 列表
     *
     * 示例：
     * GET /api/farms/batches
     * GET /api/farms/batches?farmId=farm_001
     * GET /api/farms/batches?cropType=Wheat
     * GET /api/farms/batches?farmId=farm_001&cropType=Wheat
     */
    @GetMapping
    public List<FarmBatchOptionResponse> listBatchOptions(
            @RequestParam(required = false) String farmId,
            @RequestParam(required = false) String cropType
    ) {
        return farmBatchService.listBatchOptions(farmId, cropType);
    }
}
