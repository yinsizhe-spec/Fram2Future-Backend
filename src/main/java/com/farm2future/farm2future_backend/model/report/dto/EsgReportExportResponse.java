package com.farm2future.farm2future_backend.model.report.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EsgReportExportResponse {

    @JsonProperty("download_url")
    private String downloadUrl;

    @JsonProperty("expires_at")
    private String expiresAt;
}