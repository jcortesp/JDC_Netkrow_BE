package com.netkrow.backend.dto;

public record FullReportDto(
        RemissionKpiDto remisiones,
        SalesKpiDto ventas,
        GlobalKpiDto global
) {}
