package com.farm2future.farm2future_backend.model.fram.controller;

import com.farm2future.farm2future_backend.model.fram.dto.FarmDataSubmitRequest;
import com.farm2future.farm2future_backend.model.fram.dto.FarmDataSubmitResponse;
import com.farm2future.farm2future_backend.model.fram.service.FarmDataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 农场数据提交接口控制器
 *
 * <p>
 * 该 Controller 主要负责处理农场数据相关的 HTTP 请求。
 * 当前提供的接口用于农场用户提交农场生产、资源使用、合规等数据。
 * </p>
 *
 * <p>
 * 请求路径前缀：/api/farms
 * </p>
 */
@RestController
@RequestMapping("/api/farms")
@RequiredArgsConstructor
public class FarmDataController {

    /**
     * 农场数据业务处理服务
     *
     * <p>
     * Controller 不直接处理业务逻辑，
     * 只负责接收请求、校验参数，并调用 Service 层完成具体业务。
     * </p>
     */
    private final FarmDataService farmDataService;

    /**
     * 提交指定农场的数据
     *
     * <p>
     * 接口地址：
     * POST /api/farms/{farmId}/data
     * </p>
     *
     * <p>
     * 示例：
     * POST /api/farms/farm_1/data
     * </p>
     *
     * @param farmId  农场 ID，从请求路径中获取
     * @param request 前端提交的农场数据，请求体中的 JSON 会自动转换为 FarmDataSubmitRequest 对象
     * @return FarmDataSubmitResponse 返回数据提交结果，例如提交记录 ID、状态、ESG 分数等
     */
    @PostMapping("/{farmId}/data")
    public FarmDataSubmitResponse submitFarmData(
            @PathVariable String farmId,
            @Valid @RequestBody FarmDataSubmitRequest request
    ) {
        // 调用 Service 层处理农场数据提交逻辑
        return farmDataService.submitFarmData(farmId, request);
    }
}