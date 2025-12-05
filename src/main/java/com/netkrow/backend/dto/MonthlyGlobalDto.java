package com.netkrow.backend.dto;

import java.math.BigDecimal;

public record MonthlyGlobalDto(
        int year,
        int month,
        BigDecimal ingresoNeto
) {}
