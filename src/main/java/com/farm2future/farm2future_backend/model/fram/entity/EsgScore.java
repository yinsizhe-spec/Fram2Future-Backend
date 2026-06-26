package com.farm2future.farm2future_backend.model.fram.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("esg_score")
public class EsgScore {
    private Long id;

    private Long farmId;

    private String period;

    private BigDecimal environmentalScore;

    private BigDecimal socialScore;

    private BigDecimal governanceScore;

    private BigDecimal totalScore;

    private LocalDateTime createdAt;

    private Integer deleted;
}
