package com.netkrow.backend.service;

import com.netkrow.backend.dto.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportService {

    private final EntityManager em;
    private final ExpenseService expenseService;

    public ReportService(EntityManager em, ExpenseService expenseService) {
        this.em = em;
        this.expenseService = expenseService;
    }

    // ============================================
    //  FULL REPORT (KPIs por rango)
    // ============================================
    @Transactional(readOnly = true)
    public FullReportDto getFullReport(LocalDateTime from, LocalDateTime to) {

        // ---------------- REMISIONES ----------------
        Query qRemCount = em.createNativeQuery("""
                SELECT COUNT(*) 
                FROM remissions r
                WHERE r.created_at BETWEEN :from AND :to
        """);
        qRemCount.setParameter("from", from);
        qRemCount.setParameter("to", to);
        long totalRemisiones = ((Number) qRemCount.getSingleResult()).longValue();

        Query qEquipos = em.createNativeQuery("""
                SELECT
                    COALESCE(COUNT(tr.id), 0),
                    COALESCE(SUM(
                        CASE 
                            WHEN tr.dado_baja = false THEN COALESCE(tr.valor, 0)
                            WHEN tr.dado_baja = true THEN COALESCE(tr.revision_valor, 0)
                            ELSE 0
                        END
                    ), 0)
                FROM remissions r
                JOIN technical_records tr ON tr.remission_id = r.id
                WHERE r.created_at BETWEEN :from AND :to
        """);
        qEquipos.setParameter("from", from);
        qEquipos.setParameter("to", to);

        Object[] eq = (Object[]) qEquipos.getSingleResult();
        long totalEquipos = eq[0] != null ? ((Number) eq[0]).longValue() : 0L;

        Query qIngRem = em.createNativeQuery("""
                SELECT COALESCE(SUM(
                    CASE
                        WHEN fecha_salida IS NOT NULL THEN COALESCE(total_value, 0)
                        ELSE COALESCE(deposit_value, 0)
                    END
                ), 0)
                FROM remissions
                WHERE created_at BETWEEN :from AND :to
        """);
        qIngRem.setParameter("from", from);
        qIngRem.setParameter("to", to);
        BigDecimal ingresosRemisiones = toBigDecimal(qIngRem.getSingleResult());

        BigDecimal ticketPromRem = totalRemisiones > 0
                ? ingresosRemisiones.divide(BigDecimal.valueOf(totalRemisiones), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // ---------------- VENTAS ----------------
        Query qVentas = em.createNativeQuery("""
                SELECT
                    COALESCE(SUM(s.sale_value), 0),
                    COUNT(*),
                    COALESCE(SUM(s.unit_qty), 0)
                FROM sales s
                WHERE s.sale_date BETWEEN :from AND :to
        """);
        qVentas.setParameter("from", from);
        qVentas.setParameter("to", to);
        Object[] vRow = (Object[]) qVentas.getSingleResult();

        BigDecimal totalVentas = toBigDecimal(vRow[0]);
        long totalTransacciones = vRow[1] != null ? ((Number) vRow[1]).longValue() : 0L;
        long productosTotales = vRow[2] != null ? ((Number) vRow[2]).longValue() : 0L;

        BigDecimal ticketPromVenta = totalTransacciones > 0
                ? totalVentas.divide(BigDecimal.valueOf(totalTransacciones), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal unidadesPromVenta = totalTransacciones > 0
                ? BigDecimal.valueOf(productosTotales)
                .divide(BigDecimal.valueOf(totalTransacciones), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal unidadesPromRem = totalRemisiones > 0
                ? BigDecimal.valueOf(productosTotales)
                .divide(BigDecimal.valueOf(totalRemisiones), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // ---------------- GLOBAL ----------------
        BigDecimal totalGastos = expenseService.getTotalExpenses(from, to);
        BigDecimal ingresosTotales = ingresosRemisiones.add(totalVentas);
        BigDecimal ingresoNeto = ingresosTotales.subtract(totalGastos);

        RemissionKpiDto remDto = new RemissionKpiDto(
                totalRemisiones,
                totalEquipos,
                ingresosRemisiones,
                ticketPromRem,
                unidadesPromRem
        );

        SalesKpiDto salesDto = new SalesKpiDto(
                totalTransacciones,
                productosTotales,
                totalVentas,
                ticketPromVenta,
                unidadesPromVenta
        );

        GlobalKpiDto globalDto = new GlobalKpiDto(
                ingresosTotales,
                totalGastos,
                ingresoNeto
        );

        return new FullReportDto(remDto, salesDto, globalDto);
    }

    // ============================================
    //  MONTHLY REPORT (últimos 12 meses)
    // ============================================
    @Transactional(readOnly = true)
    public MonthlyReportDto getMonthlyReport() {

        List<MonthlyRemissionDto> remList = new ArrayList<>();
        List<MonthlySalesDto> salesList = new ArrayList<>();
        List<MonthlyGlobalDto> globalList = new ArrayList<>();

        for (int i = 11; i >= 0; i--) {
            LocalDateTime start = LocalDateTime.now().minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0);
            LocalDateTime end = start.plusMonths(1).minusSeconds(1);

            int year = start.getYear();
            int month = start.getMonthValue();

            // Remisiones del mes
            Query qRem = em.createNativeQuery("""
                    SELECT
                        COALESCE(SUM(
                            CASE
                                WHEN fecha_salida IS NOT NULL THEN COALESCE(total_value, 0)
                                ELSE COALESCE(deposit_value, 0)
                            END
                        ), 0),
                        COUNT(*)
                    FROM remissions
                    WHERE created_at BETWEEN :from AND :to
            """);
            qRem.setParameter("from", start);
            qRem.setParameter("to", end);

            Object[] r = (Object[]) qRem.getSingleResult();
            BigDecimal ingRem = toBigDecimal(r[0]);
            long countRem = ((Number) r[1]).longValue();

            BigDecimal ticketRem = countRem > 0
                    ? ingRem.divide(BigDecimal.valueOf(countRem), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            remList.add(new MonthlyRemissionDto(year, month, ingRem, ticketRem));

            // Ventas del mes
            Query qSales = em.createNativeQuery("""
                    SELECT COALESCE(SUM(s.sale_value), 0), COUNT(*)
                    FROM sales s
                    WHERE sale_date BETWEEN :from AND :to
            """);
            qSales.setParameter("from", start);
            qSales.setParameter("to", end);

            Object[] sv = (Object[]) qSales.getSingleResult();
            BigDecimal ingSales = toBigDecimal(sv[0]);
            long countSales = ((Number) sv[1]).longValue();

            BigDecimal ticketSales = countSales > 0
                    ? ingSales.divide(BigDecimal.valueOf(countSales), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            salesList.add(new MonthlySalesDto(year, month, ingSales, ticketSales));

            // Global
            BigDecimal gastosMes = expenseService.getTotalExpenses(start, end);
            BigDecimal ingresoNeto = ingRem.add(ingSales).subtract(gastosMes);

            globalList.add(new MonthlyGlobalDto(year, month, ingresoNeto));
        }

        return new MonthlyReportDto(remList, salesList, globalList);
    }

    private BigDecimal toBigDecimal(Object raw) {
        if (raw == null) return BigDecimal.ZERO;
        if (raw instanceof BigDecimal b) return b;
        if (raw instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return new BigDecimal(raw.toString());
    }
}
