package com.netkrow.backend.dto;

import java.math.BigDecimal;

/**
 * KPIs globales (ingresos totales, gastos, neto).
 */
public record GlobalKpiDto(
        BigDecimal ingresosTotales,
        BigDecimal totalGastos,
        BigDecimal ingresoNeto
) {}
