package com.farm2future.farm2future_backend.model.report.controller;

import com.farm2future.farm2future_backend.model.report.dto.EsgReportGenerateRequest;
import com.farm2future.farm2future_backend.model.report.dto.EsgReportGenerateResponse;
import com.farm2future.farm2future_backend.model.report.service.EsgReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports/esg")
@RequiredArgsConstructor
public class EsgReportController {
    private final EsgReportService esgReportService;

    @PostMapping("/generate")
    public EsgReportGenerateResponse generate(@Valid @RequestBody EsgReportGenerateRequest request) {
        return esgReportService.generate(request);
    }
}
