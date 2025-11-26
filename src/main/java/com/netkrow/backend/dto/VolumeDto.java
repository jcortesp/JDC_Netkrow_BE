// src/main/java/com/netkrow/backend/dto/VolumeDto.java
package com.netkrow.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Fila del reporte por día y estado de remisión.
 * - fecha: día (yyyy-MM-dd)
 * - estado: 'Entregado' | 'Pendiente'
 * - totalRemisiones: # de remisiones en ese día/estado
 * - totalValor: dinero de remisiones en ese día/estado
 *   (si pendiente -> suma de deposit_value, si entregado -> suma de total_value)
 */
public record VolumeDto(
        LocalDate fecha,
        String estado,
        long totalRemisiones,
        BigDecimal totalValor
) {}
