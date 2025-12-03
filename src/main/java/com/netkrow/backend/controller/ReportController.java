package com.netkrow.backend.controller;

import com.netkrow.backend.dto.FullReportDto;
import com.netkrow.backend.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reports/remissions")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Endpoint único de reporte:
     * GET /api/reports/remissions/summary?from=...&to=...
     *
     * Devuelve todos los KPIs (remisiones, ventas, global).
     */
    @GetMapping("/summary")
    public FullReportDto getFullReport(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return reportService.getFullReport(from, to);
    }
}
