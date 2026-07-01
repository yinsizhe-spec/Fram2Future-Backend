package com.farm2future.farm2future_backend.model.fram.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dashboard_alert")
public class DashboardAlert {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    /**
     * 实体名称，例如 Green Valley Farm
     */
    private String entity;

    /**
     * normal / flagged / at-risk / warning / danger
     */
    private String severity;

    private LocalDateTime alertTime;

    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}