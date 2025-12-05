package com.netkrow.backend.dto;

import java.math.BigDecimal;

public record MonthlySalesDto(
        int year,
        int month,
        BigDecimal ingresosVentas,
        BigDecimal ticketPromedio
) {}
