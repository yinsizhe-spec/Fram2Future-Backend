package com.farm2future.farm2future_backend.model.fram.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 交易记录实体类
 *
 * <p>
 * 该类对应数据库中的 transaction_record 表。
 * 用于保存系统中的交易流水记录，例如 Token 发行、转移、兑换等操作。
 * </p>
 *
 * <p>
 * 如果项目后续接入区块链智能合约，
 * 该表可以用于保存链上交易哈希、交易状态和交易时间。
 * </p>
 */
@Data
@TableName("transaction_record")
public class TransactionRecord {

    /**
     * 交易记录 ID
     *
     * <p>
     * 对应数据库字段：id
     * 当前使用 IdType.INPUT，表示 ID 由程序手动生成并写入，
     * MyBatis-Plus 不会自动生成。
     * </p>
     *
     * <p>
     * 示例：tx_001
     * </p>
     */
    @TableId(type = IdType.INPUT)
    private String id;

    /**
     * Token ID
     *
     * <p>
     * 对应数据库字段：token_id
     * 用于关联 token_record 表中的 Token 记录。
     * </p>
     */
    private String tokenId;

    /**
     * 交易发起方
     *
     * <p>
     * 对应数据库字段：from_party
     * 表示 Token 或资产从哪一方转出。
     * </p>
     *
     * <p>
     * 示例：farm_1、buyer_1、system
     * </p>
     */
    private String fromParty;

    /**
     * 交易接收方
     *
     * <p>
     * 对应数据库字段：to_party
     * 表示 Token 或资产转入到哪一方。
     * </p>
     *
     * <p>
     * 示例：buyer_1、regulator_1、system
     * </p>
     */
    private String toParty;

    /**
     * 交易类型
     *
     * <p>
     * 对应数据库字段：tx_type
     * 用于区分不同类型的交易操作。
     * </p>
     *
     * <p>
     * 示例：
     * ISSUE 表示发行 Token，
     * TRANSFER 表示转移 Token，
     * REDEEM 表示兑换 Token。
     * </p>
     */
    private String txType;

    /**
     * 区块链交易哈希
     *
     * <p>
     * 对应数据库字段：tx_hash
     * 用于保存链上交易返回的哈希值。
     * 如果当前阶段还没有真实上链，可以先保存模拟哈希。
     * </p>
     *
     * <p>
     * 示例：0xabc123...
     * </p>
     */
    private String txHash;

    /**
     * 交易时间
     *
     * <p>
     * 对应数据库字段：tx_date
     * 表示该交易发生或被记录的时间。
     * </p>
     */
    private LocalDateTime txDate;

    /**
     * 交易状态
     *
     * <p>
     * 对应数据库字段：status
     * 用于标识交易当前状态。
     * </p>
     *
     * <p>
     * 示例：
     * PENDING 表示处理中，
     * SUCCESS 表示成功，
     * FAILED 表示失败。
     * </p>
     */
    private String status;

    /**
     * 逻辑删除标记
     *
     * <p>
     * 对应数据库字段：deleted
     * 一般约定：
     * 0 表示未删除
     * 1 表示已删除。
     * </p>
     */
    private Integer deleted;

    /**
     * 创建时间
     *
     * <p>
     * 对应数据库字段：create_time
     * 记录该交易记录第一次创建的时间。
     * </p>
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     *
     * <p>
     * 对应数据库字段：update_time
     * 记录该交易记录最近一次修改的时间。
     * </p>
     */
    private LocalDateTime updateTime;
}