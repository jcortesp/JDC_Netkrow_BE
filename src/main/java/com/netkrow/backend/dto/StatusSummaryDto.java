// src/main/java/com/netkrow/backend/dto/StatusSummaryDto.java
package com.netkrow.backend.dto;

import java.math.BigDecimal;

/**
 * Resumen consolidado por estado de remisión (en el rango de fechas).
 * - estado: 'Entregado' | 'Pendiente'
 * - totalRemisiones: # remisiones en ese estado
 * - totalEquipos: # equipos (technical_records) asociados a esas remisiones
 * - totalValor: dinero total asociado a esas remisiones según regla:
 *      Pendiente  -> deposit_value
 *      Entregado  -> total_value
 */
public record StatusSummaryDto(
        String estado,
        long totalRemisiones,
        long totalEquipos,
        BigDecimal totalValor
) {}
