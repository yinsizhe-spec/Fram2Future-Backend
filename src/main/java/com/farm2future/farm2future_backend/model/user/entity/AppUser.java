package com.farm2future.farm2future_backend.model.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("app_user")
public class AppUser {
    @TableId(type = IdType.INPUT)
    private String id;

    private String name;

    private String email;

    private String password;

    private String role;

    private String entityName;

    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
