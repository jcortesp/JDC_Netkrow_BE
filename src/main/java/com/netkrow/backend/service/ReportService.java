// src/main/java/com/netkrow/backend/service/ReportService.java
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
            String equipo,
            String estado
    ) {
        String sql =
                "SELECT " +
                        "  DATE(created_at) AS fecha, " +
                        "  equipo, " +
                        "  CASE WHEN fecha_salida IS NOT NULL THEN 'Entregado' ELSE 'Pendiente' END AS estado, " +
                        "  COUNT(*) AS total_remisiones, " +
                        "  SUM(total_value) AS total_valor " +
                        "FROM remissions " +
                        "WHERE created_at BETWEEN :from AND :to " +
                        // CAST(:equipo AS text) en lugar de :equipo::text
                        "  AND ( CAST(:equipo AS text) IS NULL OR equipo = CAST(:equipo AS text) ) " +
                        "  AND ( CAST(:estado AS text) IS NULL " +
                        "     OR (fecha_salida IS NOT NULL AND CAST(:estado AS text) = 'Entregado') " +
                        "     OR (fecha_salida IS NULL     AND CAST(:estado AS text) = 'Pendiente') ) " +
                        "GROUP BY DATE(created_at), equipo, estado " +
                        "ORDER BY DATE(created_at), equipo, estado";

        Query q = em.createNativeQuery(sql);
        q.setParameter("from",   from);
        q.setParameter("to",     to);
        q.setParameter("equipo", equipo);
        q.setParameter("estado", estado);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();

        return rows.stream().map(r -> {
            java.sql.Date fechaSql = (java.sql.Date) r[0];
            String        eq      = (String)       r[1];
            String        st      = (String)       r[2];
            long          cnt     = ((Number)      r[3]).longValue();

            Object rawTotal = r[4];
            BigDecimal totalVal;
            if (rawTotal instanceof BigDecimal) {
                totalVal = (BigDecimal) rawTotal;
            } else {
                totalVal = BigDecimal.valueOf(((Number) rawTotal).doubleValue());
            }

            return new VolumeDto(
                    fechaSql.toLocalDate(),
                    eq,
                    st,
                    cnt,
                    totalVal
            );
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getDistinctEquipos() {
        @SuppressWarnings("unchecked")
        List<String> equipos = em.createNativeQuery(
                "SELECT DISTINCT equipo FROM remissions WHERE equipo IS NOT NULL ORDER BY equipo"
        ).getResultList();
        return equipos;
    }
}
