// src/main/java/com/netkrow/backend/dto/IncomeSummaryDto.java
package com.netkrow.backend.dto;

import java.math.BigDecimal;

/**
 * Resumen de ingresos reales:
 * - totalAbonosPendientes: suma de deposit_value de remisiones pendientes
 * - totalPagosEntregadas: suma de total_value de remisiones entregadas
 * - totalVentas: suma de sale_value en tabla sales
 * - totalIngresosReales: suma de los tres anteriores
 */
public record IncomeSummaryDto(
        BigDecimal totalAbonosPendientes,
        BigDecimal totalPagosEntregadas,
        BigDecimal totalVentas,
        BigDecimal totalIngresosReales
) {}
