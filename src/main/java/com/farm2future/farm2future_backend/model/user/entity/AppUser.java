package com.farm2future.farm2future_backend.model.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类。
 *
 * <p>
 * 该类对应数据库中的 app_user 表，
 * 用于表示系统中的登录用户。
 * </p>
 *
 * <p>
 * 当前系统中，不同角色的用户都可以存放在该表中，
 * 例如 farmer、regulator、admin。
 * </p>
 *
 * <p>
 * 该实体类主要用于 MyBatis-Plus 和数据库进行映射。
 * </p>
 */
@Data
@TableName("app_user")
public class AppUser {

    /**
     * 用户 ID。
     *
     * <p>
     * 对应数据库 app_user 表中的 id 字段。
     * </p>
     *
     * <p>
     * {@code IdType.INPUT} 表示主键 ID 由程序手动传入，
     * 而不是数据库自动生成。
     * </p>
     */
    @TableId(type = IdType.INPUT)
    private String id;

    /**
     * 用户姓名。
     *
     * <p>
     * 用于前端展示当前登录用户名称。
     * </p>
     */
    private String name;

    /**
     * 用户邮箱。
     *
     * <p>
     * 用作登录账号。
     * 建议数据库中对 email 字段建立唯一索引，
     * 或者建立 email + role 的联合唯一索引。
     * </p>
     */
    private String email;

    /**
     * 用户密码。
     *
     * <p>
     * 数据库中保存的不是明文密码，
     * 而是 BCrypt 加密后的密码哈希。
     * </p>
     *
     * <p>
     * 登录时使用 PasswordEncoder.matches()
     * 将前端传入的明文密码与该字段进行匹配。
     * </p>
     */
    private String password;

    /**
     * 用户角色。
     *
     * <p>
     * 用于区分当前用户身份和权限。
     * </p>
     *
     * <p>
     * 当前登录逻辑中允许的角色包括：
     * farmer、regulator、admin。
     * </p>
     */
    private String role;

    /**
     * 用户所属实体名称。
     *
     * <p>
     * 例如：
     * 农场用户可以对应农场名称，
     * 监管用户可以对应监管机构名称，
     * 管理员可以对应平台名称。
     * </p>
     */
    private String entityName;

    /**
     * 逻辑删除标记。
     *
     * <p>
     * 0 表示未删除；
     * 1 表示已删除。
     * </p>
     *
     * <p>
     * 当前登录查询中会限制 deleted = 0，
     * 表示已删除用户不能登录系统。
     * </p>
     */
    private Integer deleted;

    /**
     * 创建时间。
     *
     * <p>
     * 表示该用户记录首次创建的时间。
     * </p>
     */
    private LocalDateTime createTime;

    /**
     * 更新时间。
     *
     * <p>
     * 表示该用户记录最后一次被修改的时间。
     * </p>
     */
    private LocalDateTime updateTime;
}