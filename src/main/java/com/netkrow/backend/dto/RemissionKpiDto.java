package com.netkrow.backend.dto;

import java.math.BigDecimal;

/**
 * KPIs de remisiones.
 */
public record RemissionKpiDto(
        long totalRemisiones,
        long totalEquipos,
        BigDecimal totalValorRemisiones,
        BigDecimal ticketPromedioRemision,
        BigDecimal unidadesPromedioPorRemision
) {}
