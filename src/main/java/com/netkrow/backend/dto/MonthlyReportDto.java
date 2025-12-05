package com.netkrow.backend.dto;

import java.util.List;

public record MonthlyReportDto(
        List<MonthlyRemissionDto> remisiones,
        List<MonthlySalesDto> ventas,
        List<MonthlyGlobalDto> global
) {}
