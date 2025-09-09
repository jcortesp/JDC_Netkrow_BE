package com.netkrow.backend.service;

import com.netkrow.backend.dto.VolumeDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final EntityManager em;

    public ReportService(EntityManager em) {
        this.em = em;
    }

    @Transactional(readOnly = true)
    public List<VolumeDto> getVolumeReport(
            LocalDateTime from,
            LocalDateTime to,
            String equipo,   // ignorado (no existe en la tabla)
            String estado    // "Entregado" | "Pendiente" | null/blank
    ) {
        // Base: agrupamos por fecha y estado (sin "equipo" porque no está en la tabla)
        StringBuilder sql = new StringBuilder()
            .append("SELECT ")
            .append("  DATE(created_at) AS fecha, ")
            .append("  CASE WHEN fecha_salida IS NOT NULL THEN 'Entregado' ELSE 'Pendiente' END AS estado, ")
            .append("  COUNT(*) AS total_remisiones, ")
            .append("  SUM(COALESCE(total_value,0)) AS total_valor ")
            .append("FROM remissions ")
            .append("WHERE created_at BETWEEN :from AND :to ");

        // Si 'estado' viene informado, aplicamos el filtro; si no, no añadimos nada
        boolean filterEstado = estado != null && !estado.isBlank();
        if (filterEstado) {
            sql.append("AND (")
               .append("     (fecha_salida IS NOT NULL AND :estado = 'Entregado') ")
               .append("  OR (fecha_salida IS NULL     AND :estado = 'Pendiente')")
               .append(") ");
        }

        sql.append("GROUP BY DATE(created_at), estado ")
           .append("ORDER BY DATE(created_at), estado");

        Query q = em.createNativeQuery(sql.toString());
        q.setParameter("from", from);
        q.setParameter("to", to);
        if (filterEstado) {
            // Solo seteamos el parámetro si lo usamos en el SQL (evita 42P18)
            q.setParameter("estado", estado);
        }

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();

        // VolumeDto = (fecha, equipo, estado, totalRemisiones, totalValor)
        // 'equipo' queda vacío porque no existe en la tabla
        return rows.stream().map(r -> {
            java.sql.Date fechaSql = (java.sql.Date) r[0];
            String st = (String) r[1];
            long cnt = ((Number) r[2]).longValue();

            Object rawTotal = r[3];
            BigDecimal totalVal = (rawTotal instanceof BigDecimal)
                    ? (BigDecimal) rawTotal
                    : BigDecimal.valueOf(((Number) rawTotal).doubleValue());

            return new VolumeDto(
                    fechaSql.toLocalDate(),
                    "",          // equipo vacío
                    st,
                    cnt,
                    totalVal
            );
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getDistinctEquipos() {
        // No hay columna 'equipo' en remissions: devolvemos vacío
        return List.of();
    }
}
