package com.netkrow.backend.service;

import com.netkrow.backend.dto.RemissionSummaryDto;
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
    private final ExpenseService expenseService;

    public ReportService(EntityManager em, ExpenseService expenseService) {
        this.em = em;
        this.expenseService = expenseService;
    }

    /**
     * Reporte de volumen por fecha y estado (remisiones),
     * mismo comportamiento que ya tenías.
     */
    @Transactional(readOnly = true)
    public List<VolumeDto> getVolumeReport(
            LocalDateTime from,
            LocalDateTime to,
            String equipo,   // ignorado (no existe en la tabla)
            String estado    // "Entregado" | "Pendiente" | null/blank
    ) {
        StringBuilder sql = new StringBuilder()
                .append("SELECT ")
                .append("  DATE(created_at) AS fecha, ")
                .append("  CASE WHEN fecha_salida IS NOT NULL THEN 'Entregado' ELSE 'Pendiente' END AS estado, ")
                .append("  COUNT(*) AS total_remisiones, ")
                .append("  SUM(COALESCE(total_value,0)) AS total_valor ")
                .append("FROM remissions ")
                .append("WHERE created_at BETWEEN :from AND :to ");

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
            q.setParameter("estado", estado);
        }

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();

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
        st,
        cnt,
        totalVal
);

        }).collect(Collectors.toList());
    }

    /**
     * Resumen: #equipos por estado, valor por estado, ingresos reales
     * (remisiones + ventas) y gastos/neto.
     */
    @Transactional(readOnly = true)
    public RemissionSummaryDto getRemissionSummary(
            LocalDateTime from,
            LocalDateTime to,
            String estado // opcional, mismo filtro de arriba
    ) {
        // 1) Equipos y valor por estado usando technical_records + remissions
        StringBuilder sqlEquipos = new StringBuilder()
                .append("SELECT ")
                .append("  CASE WHEN r.fecha_salida IS NOT NULL THEN 'Entregado' ELSE 'Pendiente' END AS estado, ")
                .append("  COUNT(tr.id) AS total_equipos, ")
                .append("  SUM( CASE ")
                .append("         WHEN tr.dado_baja = false THEN COALESCE(tr.valor, 0) ")
                .append("         WHEN tr.dado_baja = true  THEN COALESCE(tr.revision_valor, 0) ")
                .append("         ELSE 0 ")
                .append("      END ) AS total_valor_equipos ")
                .append("FROM remissions r ")
                .append("JOIN technical_records tr ON tr.remission_id = r.id ")
                .append("WHERE r.created_at BETWEEN :from AND :to ");

        boolean filterEstado = estado != null && !estado.isBlank();
        if (filterEstado) {
            sqlEquipos.append("AND (")
                    .append("     (r.fecha_salida IS NOT NULL AND :estado = 'Entregado') ")
                    .append("  OR (r.fecha_salida IS NULL     AND :estado = 'Pendiente')")
                    .append(") ");
        }

        sqlEquipos.append("GROUP BY estado");

        Query qEquipos = em.createNativeQuery(sqlEquipos.toString());
        qEquipos.setParameter("from", from);
        qEquipos.setParameter("to", to);
        if (filterEstado) {
            qEquipos.setParameter("estado", estado);
        }

        long equiposPendientes = 0L;
        long equiposEntregados = 0L;
        BigDecimal valorPend = BigDecimal.ZERO;
        BigDecimal valorEnt = BigDecimal.ZERO;

        @SuppressWarnings("unchecked")
        List<Object[]> equiposRows = qEquipos.getResultList();
        for (Object[] row : equiposRows) {
            String st = (String) row[0];
            long count = ((Number) row[1]).longValue();
            Object rawVal = row[2];
            BigDecimal totalVal = (rawVal instanceof BigDecimal)
                    ? (BigDecimal) rawVal
                    : BigDecimal.valueOf(((Number) rawVal).doubleValue());

            if ("Entregado".equalsIgnoreCase(st)) {
                equiposEntregados += count;
                valorEnt = valorEnt.add(totalVal);
            } else {
                equiposPendientes += count;
                valorPend = valorPend.add(totalVal);
            }
        }

        // 2) Ingresos reales por remisiones (pendiente -> deposit_value, entregado -> total_value)
        StringBuilder sqlRemisiones = new StringBuilder()
                .append("SELECT ")
                .append("  SUM( CASE ")
                .append("         WHEN fecha_salida IS NOT NULL THEN COALESCE(total_value, 0) ")
                .append("         ELSE COALESCE(deposit_value, 0) ")
                .append("      END ) AS ingresos_remisiones ")
                .append("FROM remissions ")
                .append("WHERE created_at BETWEEN :from AND :to ");

        if (filterEstado) {
            sqlRemisiones.append("AND (")
                    .append("     (fecha_salida IS NOT NULL AND :estado = 'Entregado') ")
                    .append("  OR (fecha_salida IS NULL     AND :estado = 'Pendiente')")
                    .append(") ");
        }

        Query qRem = em.createNativeQuery(sqlRemisiones.toString());
        qRem.setParameter("from", from);
        qRem.setParameter("to", to);
        if (filterEstado) {
            qRem.setParameter("estado", estado);
        }

        BigDecimal ingresosRemisiones = toBigDecimal(qRem.getSingleResult());

        // 3) Ingresos por ventas
        String sqlVentas = """
                SELECT COALESCE(SUM(s.sale_value), 0)
                FROM sales s
                WHERE s.sale_date BETWEEN :from AND :to
                """;
        Query qVentas = em.createNativeQuery(sqlVentas);
        qVentas.setParameter("from", from);
        qVentas.setParameter("to", to);
        BigDecimal ingresosVentas = toBigDecimal(qVentas.getSingleResult());

        // 4) Total gastos (via ExpenseService)
        BigDecimal totalGastos = expenseService.getTotalExpenses(from, to);

        // 5) Total ingresos y neto
        BigDecimal ingresosTotales = ingresosRemisiones.add(ingresosVentas);
        BigDecimal ingresoNeto = ingresosTotales.subtract(totalGastos);

        return new RemissionSummaryDto(
                equiposPendientes,
                equiposEntregados,
                valorPend,
                valorEnt,
                ingresosRemisiones,
                ingresosVentas,
                ingresosTotales,
                totalGastos,
                ingresoNeto
        );
    }

    @Transactional(readOnly = true)
    public List<String> getDistinctEquipos() {
        // De momento seguimos devolviendo lista vacía
        return List.of();
    }

    // Helper seguro para convertir cualquier Number/BigDecimal a BigDecimal
    private BigDecimal toBigDecimal(Object raw) {
        if (raw == null) return BigDecimal.ZERO;
        if (raw instanceof BigDecimal bd) return bd;
        if (raw instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return new BigDecimal(raw.toString());
    }
}
