package com.farm2future.farm2future_backend.model.fram.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("dashboard_chart_point")
public class DashboardChartPoint {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 例如 All Farms
     */
    private String entity;

    /**
     * 例如 Jan / Feb / Jul
     */
    private String chartMonth;

    private BigDecimal chartValue;

    private Integer chartYear;

    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
