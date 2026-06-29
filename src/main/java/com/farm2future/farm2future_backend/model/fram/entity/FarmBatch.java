package com.farm2future.farm2future_backend.model.fram.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 农场批次数据实体类
 *
 * <p>
 * 该类对应数据库中的 farm_batch 表。
 * 用于保存农场提交的某一次农作物批次数据，
 * 包括作物类型、产量、资源使用、销售、成本以及区块链交易哈希等信息。
 * </p>
 */
@Data
@TableName("farm_batch")
public class FarmBatch {

    /**
     * 批次 ID
     *
     * <p>
     * 对应数据库字段：id
     * 当前使用 IdType.INPUT，表示 ID 由程序手动生成并写入，
     * MyBatis-Plus 不会自动生成。
     * </p>
     *
     * <p>
     * 示例：batch_001
     * </p>
     */
    @TableId(type = IdType.INPUT)
    private String id;

    /**
     * 农场 ID
     *
     * <p>
     * 对应数据库字段：farm_id
     * 用于关联 farm 表中的农场记录。
     * </p>
     */
    private String farmId;

    /**
     * 作物类型
     *
     * <p>
     * 对应数据库字段：crop_type
     * 例如：Rice、Corn、Vegetables。
     * </p>
     */
    private String cropType;

    /**
     * 批次日期
     *
     * <p>
     * 对应数据库字段：batch_date
     * 表示该批次农场数据所属的日期。
     * </p>
     */
    private LocalDate batchDate;

    /**
     * 农作物产量，单位：kg
     *
     * <p>
     * 对应数据库字段：yield_kg
     * </p>
     */
    private BigDecimal yieldKg;

    /**
     * 用水量，单位：L
     *
     * <p>
     * 对应数据库字段：water_usage_l
     * </p>
     */
    private BigDecimal waterUsageL;

    /**
     * 肥料类型
     *
     * <p>
     * 对应数据库字段：fertiliser_type
     * 例如：Organic、Chemical、Mixed。
     * </p>
     */
    private String fertiliserType;

    /**
     * 肥料使用量，单位：kg
     *
     * <p>
     * 对应数据库字段：fertiliser_usage_kg
     * </p>
     */
    private BigDecimal fertiliserUsageKg;

    /**
     * 销售数量，单位：kg
     *
     * <p>
     * 对应数据库字段：sale_quantity_kg
     * </p>
     */
    private BigDecimal saleQuantityKg;

    /**
     * 销售单价，单位：RM/kg
     *
     * <p>
     * 对应数据库字段：sale_unit_price_rm
     * </p>
     */
    private BigDecimal saleUnitPriceRm;

    /**
     * 买家名称
     *
     * <p>
     * 对应数据库字段：buyer_name
     * 例如：Green Market Sdn Bhd。
     * </p>
     */
    private String buyerName;

    /**
     * 种子成本，单位：RM
     *
     * <p>
     * 对应数据库字段：seed_cost_rm
     * </p>
     */
    private BigDecimal seedCostRm;

    /**
     * 肥料成本，单位：RM
     *
     * <p>
     * 对应数据库字段：fertiliser_cost_rm
     * </p>
     */
    private BigDecimal fertiliserCostRm;

    /**
     * 区块链交易哈希
     *
     * <p>
     * 对应数据库字段：tx_hash
     * 用于记录该批次数据上链后的交易哈希。
     * 如果当前还没有接入真实智能合约，可以先保存模拟哈希。
     * </p>
     */
    private String txHash;

    /**
     * 数据提交时间
     *
     * <p>
     * 对应数据库字段：submitted_at
     * 表示该批次数据被提交到系统的时间。
     * </p>
     */
    private LocalDateTime submittedAt;

    /**
     * 逻辑删除标记
     *
     * <p>
     * 对应数据库字段：deleted
     * 一般约定：
     * 0 表示未删除
     * 1 表示已删除
     * </p>
     */
    private Integer deleted;

    /**
     * 创建时间
     *
     * <p>
     * 对应数据库字段：create_time
     * 记录该批次数据第一次创建的时间。
     * </p>
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     *
     * <p>
     * 对应数据库字段：update_time
     * 记录该批次数据最近一次修改的时间。
     * </p>
     */
    private LocalDateTime updateTime;
}