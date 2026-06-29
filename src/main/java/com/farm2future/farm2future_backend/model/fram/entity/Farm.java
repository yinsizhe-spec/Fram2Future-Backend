package com.farm2future.farm2future_backend.model.fram.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 农场实体类
 *
 * <p>
 * 该类对应数据库中的 farm 表。
 * 用于保存农场的基础信息，例如农场名称、负责人、位置等。
 * </p>
 */
@Data
@TableName("farm")
public class Farm {

    /**
     * 农场 ID
     *
     * <p>
     * 对应数据库字段：id
     * 当前使用 IdType.INPUT，表示 ID 由程序手动传入，
     * MyBatis-Plus 不会自动生成 ID。
     * </p>
     *
     * <p>
     * 示例：farm_1
     * </p>
     */
    @TableId(type = IdType.INPUT)
    private String id;

    /**
     * 农场名称
     *
     * <p>
     * 对应数据库字段：farm_name
     * </p>
     */
    private String farmName;

    /**
     * 农场所属用户 ID
     *
     * <p>
     * 对应数据库字段：owner_user_id
     * 通常关联 app_user 表中的用户 ID。
     * </p>
     */
    private String ownerUserId;

    /**
     * 农场位置
     *
     * <p>
     * 对应数据库字段：location
     * 例如：Selangor, Malaysia
     * </p>
     */
    private String location;

    /**
     * 农场负责人名称
     *
     * <p>
     * 对应数据库字段：owner_name
     * 例如：Joseph
     * </p>
     */
    private String ownerName;

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
     * 记录该农场信息第一次创建的时间。
     * </p>
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     *
     * <p>
     * 对应数据库字段：update_time
     * 记录该农场信息最近一次修改的时间。
     * </p>
     */
    private LocalDateTime updateTime;
}