package com.netkrow.backend.controller;

import com.netkrow.backend.dto.RemissionSummaryDto;
import com.netkrow.backend.dto.VolumeDto;
import com.netkrow.backend.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reports/remissions")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/volume")
    public List<VolumeDto> getVolumeReport(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String equipo // aceptado pero ignorado
    ) {
        return reportService.getVolumeReport(from, to, null, estado);
    }

    @GetMapping("/summary")
    public RemissionSummaryDto getRemissionSummary(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String estado
    ) {
        return reportService.getRemissionSummary(from, to, estado);
    }

    @GetMapping("/equipos")
    public List<String> getEquipos() {
        return reportService.getDistinctEquipos();
    }
}
