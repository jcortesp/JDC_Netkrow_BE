package com.netkrow.backend.dto;

import java.math.BigDecimal;

public record MonthlyRemissionDto(
        int year,
        int month,
        BigDecimal ingresosRemisiones,
        BigDecimal ticketPromedio
) {}
