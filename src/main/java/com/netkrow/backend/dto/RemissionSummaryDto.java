package com.netkrow.backend.dto;

import java.math.BigDecimal;

public record RemissionSummaryDto(
        long equiposPendientes,
        long equiposEntregados,
        BigDecimal valorEquiposPendientes,
        BigDecimal valorEquiposEntregados,
        BigDecimal ingresosRemisiones,
        BigDecimal ingresosVentas,
        BigDecimal ingresosTotales,
        BigDecimal totalGastos,
        BigDecimal ingresoNeto
) {}
