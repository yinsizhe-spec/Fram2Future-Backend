package com.farm2future.farm2future_backend.model.fram.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("esg_score")
public class EsgScore {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String farmId;

    private BigDecimal environmentalScore;

    private BigDecimal socialScore;

    private BigDecimal governanceScore;

    private BigDecimal overallScore;

    private String grade;

    private String suggestion;

    private Integer anomalyDetected;

    private Integer esgCompliant;

    private BigDecimal suggestedTokenAmount;

    private String period;

    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}