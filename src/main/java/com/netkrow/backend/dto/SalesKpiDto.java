package com.netkrow.backend.dto;

import java.math.BigDecimal;

/**
 * KPIs de ventas.
 */
public record SalesKpiDto(
        long totalTransacciones,
        long productosTotales,
        BigDecimal totalVentas,
        BigDecimal ticketPromedioVenta,
        BigDecimal unidadesPromedioPorVenta
) {}
