package com.farm2future.farm2future_backend.model.report.service;

import com.farm2future.farm2future_backend.model.report.dto.EsgReportExportResponse;
import com.farm2future.farm2future_backend.model.report.dto.EsgReportGenerateRequest;
import com.farm2future.farm2future_backend.model.report.dto.EsgReportGenerateResponse;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EsgReportExportService {

    private final EsgReportService esgReportService;

    private static final Path EXPORT_DIR = Paths.get(
            System.getProperty("java.io.tmpdir"),
            "farm2future-esg-reports"
    );

    public EsgReportExportResponse export(
            String format,
            String farmId,
            String from,
            HttpServletRequest servletRequest
    ) {
        String normalizedFormat = normalizeFormat(format);

        EsgReportGenerateRequest request = buildGenerateRequest(farmId, from);

        EsgReportGenerateResponse report = esgReportService.generate(request);

        try {
            Files.createDirectories(EXPORT_DIR);

            String filename = buildFilename(normalizedFormat, farmId, from);
            Path filePath = EXPORT_DIR.resolve(filename);

            if ("csv".equals(normalizedFormat)) {
                writeCsv(filePath, report);
            } else if ("pdf".equals(normalizedFormat)) {
                writePdf(filePath, report);
            } else {
                throw new IllegalArgumentException("Unsupported export format: " + format);
            }

            String downloadUrl = buildDownloadUrl(servletRequest, filename);

            String expiresAt = Instant.now()
                    .plus(1, ChronoUnit.HOURS)
                    .toString();

            return new EsgReportExportResponse(downloadUrl, expiresAt);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export ESG report", e);
        }
    }

    private EsgReportGenerateRequest buildGenerateRequest(String farmId, String from) {
        LocalDate inputDate;

        if (from == null || from.isBlank()) {
            inputDate = LocalDate.now();
        } else {
            inputDate = LocalDate.parse(from);
        }

        LocalDate quarterStartDate = getQuarterStartDate(inputDate);
        LocalDate quarterEndDate = getQuarterEndDate(inputDate);

        EsgReportGenerateRequest request = new EsgReportGenerateRequest();
        request.setFrom(quarterStartDate.toString());
        request.setTo(quarterEndDate.toString());

        if (farmId == null || farmId.isBlank()) {
            request.setEntity("All Entities");
        } else {
            request.setEntity(farmId);
        }

        return request;
    }

    private LocalDate getQuarterStartDate(LocalDate date) {
        int month = date.getMonthValue();
        int quarter = ((month - 1) / 3) + 1;

        int startMonth = (quarter - 1) * 3 + 1;

        return LocalDate.of(date.getYear(), startMonth, 1);
    }

    private LocalDate getQuarterEndDate(LocalDate date) {
        int month = date.getMonthValue();
        int quarter = ((month - 1) / 3) + 1;

        int endMonth = quarter * 3;

        return LocalDate.of(date.getYear(), endMonth, 1)
                .withDayOfMonth(
                        LocalDate.of(date.getYear(), endMonth, 1).lengthOfMonth()
                );
    }

    public ResponseEntity<Resource> download(String filename) {
        if (filename == null
                || filename.isBlank()
                || filename.contains("..")
                || filename.contains("/")
                || filename.contains("\\")) {
            return ResponseEntity.badRequest().build();
        }

        Path filePath = EXPORT_DIR.resolve(filename).normalize();

        if (!filePath.startsWith(EXPORT_DIR)) {
            return ResponseEntity.badRequest().build();
        }

        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(filePath);

        String contentType;

        if (filename.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            contentType = "text/csv; charset=UTF-8";
        } else if (filename.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            contentType = "application/pdf";
        } else {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    private String normalizeFormat(String format) {
        if (format == null || format.isBlank()) {
            throw new IllegalArgumentException("format is required");
        }

        String normalized = format.trim().toLowerCase(Locale.ROOT);

        if (!"csv".equals(normalized) && !"pdf".equals(normalized)) {
            throw new IllegalArgumentException("format must be csv or pdf");
        }

        return normalized;
    }

    private String buildFilename(String format, String farmId, String from) {
        String finalFarmId;

        if (farmId == null || farmId.isBlank()) {
            finalFarmId = "All Entities";
        } else {
            finalFarmId = farmId;
        }

        String finalFrom;

        if (from == null || from.isBlank()) {
            finalFrom = LocalDate.now().toString();
        } else {
            finalFrom = from;
        }

        String safeFarmId = finalFarmId.replaceAll("[^a-zA-Z0-9_-]", "_");

        return "esg-report-"
                + safeFarmId
                + "-"
                + finalFrom
                + "-"
                + Instant.now().toEpochMilli()
                + "-"
                + UUID.randomUUID()
                + "."
                + format;
    }

    private String buildDownloadUrl(HttpServletRequest request, String filename) {
        String baseUrl = request.getRequestURL()
                .toString()
                .replace("/export", "/export/download/");

        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replace("+", "%20");

        return baseUrl + encodedFilename;
    }

    private void writeCsv(Path filePath, EsgReportGenerateResponse report) throws Exception {
        StringBuilder csv = new StringBuilder();

        csv.append('\uFEFF');

        csv.append("Section,Field,Value\n");

        csv.append(csvRow("Report", "Entity", report.getEntity()));
        csv.append(csvRow("Report", "Period From", report.getPeriod().getFrom()));
        csv.append(csvRow("Report", "Period To", report.getPeriod().getTo()));
        csv.append(csvRow("Report", "Generated At", report.getGeneratedAt()));

        csv.append("\n");

        csv.append("Score Label,Score,Note\n");

        if (report.getScores() != null) {
            for (EsgReportGenerateResponse.ScoreItem score : report.getScores()) {
                csv.append(csvRow(
                        score.getLabel(),
                        String.valueOf(score.getScore()),
                        score.getNote()
                ));
            }
        }

        csv.append("\n");

        csv.append("Risk Type,Risk Title,Risk Description\n");

        if (report.getRiskFlags() != null) {
            for (EsgReportGenerateResponse.RiskFlag riskFlag : report.getRiskFlags()) {
                csv.append(csvRow(
                        riskFlag.getType(),
                        riskFlag.getTitle(),
                        riskFlag.getDesc()
                ));
            }
        }

        Files.writeString(
                filePath,
                csv.toString(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    private void writePdf(Path filePath, EsgReportGenerateResponse report) throws Exception {
        try (OutputStream outputStream = Files.newOutputStream(
                filePath,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        )) {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);

            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font headingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

            document.add(new Paragraph("Farm2Future ESG Report", titleFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Report Information", headingFont));
            document.add(new Paragraph("Farm ID / Entity: " + safe(report.getEntity()), normalFont));
            document.add(new Paragraph("Period: " + safe(report.getPeriod().getFrom()) + " to " + safe(report.getPeriod().getTo()), normalFont));
            document.add(new Paragraph("Generated At: " + safe(report.getGeneratedAt()), normalFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("ESG Scores", headingFont));

            if (report.getScores() != null && !report.getScores().isEmpty()) {
                for (EsgReportGenerateResponse.ScoreItem score : report.getScores()) {
                    document.add(new Paragraph(
                            safe(score.getLabel()) + ": " + safe(score.getScore()),
                            normalFont
                    ));
                    document.add(new Paragraph("Note: " + safe(score.getNote()), normalFont));
                    document.add(new Paragraph(" "));
                }
            } else {
                document.add(new Paragraph("No ESG score data available.", normalFont));
            }

            document.add(new Paragraph("Risk Flags", headingFont));

            if (report.getRiskFlags() != null && !report.getRiskFlags().isEmpty()) {
                for (EsgReportGenerateResponse.RiskFlag riskFlag : report.getRiskFlags()) {
                    document.add(new Paragraph(
                            "[" + safe(riskFlag.getType()) + "] " + safe(riskFlag.getTitle()),
                            normalFont
                    ));
                    document.add(new Paragraph(safe(riskFlag.getDesc()), normalFont));
                    document.add(new Paragraph(" "));
                }
            } else {
                document.add(new Paragraph("No risk flags available.", normalFont));
            }

            document.close();
        }
    }

    private String csvRow(String first, String second, String third) {
        return csvEscape(first) + "," + csvEscape(second) + "," + csvEscape(third) + "\n";
    }

    private String csvEscape(String value) {
        if (value == null) {
            return "";
        }

        String escaped = value.replace("\"", "\"\"");

        return "\"" + escaped + "\"";
    }

    private String safe(Object value) {
        if (value == null) {
            return "";
        }

        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal.stripTrailingZeros().toPlainString();
        }

        return String.valueOf(value);
    }
}