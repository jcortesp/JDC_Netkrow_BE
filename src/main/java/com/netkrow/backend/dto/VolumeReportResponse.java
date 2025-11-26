// src/main/java/com/netkrow/backend/dto/VolumeReportResponse.java
package com.netkrow.backend.dto;

import java.util.List;

/**
 * Respuesta completa del endpoint /api/reports/remissions/volume
 *
 * - rows: lista de VolumeDto (detalle por fecha y estado)
 * - estados: resumen consolidado por estado (StatusSummaryDto)
 * - ingresos: resumen de ingresos reales (IncomeSummaryDto)
 */
public record VolumeReportResponse(
        List<VolumeDto> rows,
        List<StatusSummaryDto> estados,
        IncomeSummaryDto ingresos
) {}
