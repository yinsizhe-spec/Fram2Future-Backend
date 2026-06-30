package com.farm2future.farm2future_backend.model.report.controller;

import com.farm2future.farm2future_backend.model.report.dto.EsgReportExportResponse;
import com.farm2future.farm2future_backend.model.report.dto.EsgReportGenerateRequest;
import com.farm2future.farm2future_backend.model.report.dto.EsgReportGenerateResponse;
import com.farm2future.farm2future_backend.model.report.service.EsgReportExportService;
import com.farm2future.farm2future_backend.model.report.service.EsgReportService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports/esg")
@RequiredArgsConstructor
public class EsgReportController {

    private final EsgReportService esgReportService;
    private final EsgReportExportService esgReportExportService;

    @PostMapping("/generate")
    public EsgReportGenerateResponse generate(@Valid @RequestBody EsgReportGenerateRequest request) {
        return esgReportService.generate(request);
    }

    @GetMapping("/export")
    public EsgReportExportResponse export(
            @RequestParam String format,
            @RequestParam(required = false) String farmId,
            @RequestParam(required = false) String from,
            HttpServletRequest servletRequest
    ) {
        return esgReportExportService.export(format, farmId, from, servletRequest);
    }

    @GetMapping("/export/download/{filename}")
    public ResponseEntity<Resource> download(@PathVariable String filename) {
        return esgReportExportService.download(filename);
    }
}