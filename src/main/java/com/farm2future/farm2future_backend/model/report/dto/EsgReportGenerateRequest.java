package com.farm2future.farm2future_backend.model.report.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EsgReportGenerateRequest {
    @NotBlank(message = "from is required")
    private String from;

    @NotBlank(message = "to is required")
    private String to;

    @NotBlank(message = "entity is required")
    private String entity;
}
