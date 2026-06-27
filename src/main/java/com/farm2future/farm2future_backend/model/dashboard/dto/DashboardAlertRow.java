package com.farm2future.farm2future_backend.model.dashboard.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DashboardAlertRow {
    private Long id;

    private String title;

    private String entity;

    private String severity;

    private LocalDateTime alertTime;
}
