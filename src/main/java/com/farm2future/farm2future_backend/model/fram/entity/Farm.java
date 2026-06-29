package com.farm2future.farm2future_backend.model.fram.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("farm")
public class Farm {
    @TableId(type = IdType.INPUT)
    private String id;

    private String farmName;

    private String ownerUserId;

    private String location;

    private String ownerName;

    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
