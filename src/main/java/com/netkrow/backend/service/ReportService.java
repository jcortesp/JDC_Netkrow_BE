package com.netkrow.backend.service;

import com.netkrow.backend.dto.FullReportDto;
import com.netkrow.backend.dto.GlobalKpiDto;
import com.netkrow.backend.dto.RemissionKpiDto;
import com.netkrow.backend.dto.SalesKpiDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class ReportService {

    private final EntityManager em;
    private final ExpenseService expenseService;

    public ReportService(EntityManager em, ExpenseService expenseService) {
        this.em = em;
        this.expenseService = expenseService;
    }

    /**
     * Reporte completo de KPIs para remisiones, ventas y global.
     */
    @Transactional(readOnly = true)
    public FullReportDto getFullReport(LocalDateTime from, LocalDateTime to) {

        // ==========================
        // 1) REMISIONES
        // ==========================

        // 1.1 Total de remisiones
        Query qRemCount = em.createNativeQuery("""
                SELECT COUNT(*) 
                FROM remissions r
                WHERE r.created_at BETWEEN :from AND :to
                """);
        qRemCount.setParameter("from", from);
        qRemCount.setParameter("to", to);
        long totalRemisiones = ((Number) qRemCount.getSingleResult()).longValue();

        // 1.2 Total equipos y valor equipos (por si luego quieres usarlo; ahora usamos solo totalEquipos)
        Query qEquipos = em.createNativeQuery("""
                SELECT
                    COALESCE(COUNT(tr.id), 0) AS total_equipos,
                    COALESCE(SUM(
                        CASE
                            WHEN tr.dado_baja = false THEN COALESCE(tr.valor, 0)
                            WHEN tr.dado_baja = true  THEN COALESCE(tr.revision_valor, 0)
                            ELSE 0
                        END
                    ), 0) AS total_valor_equipos
                FROM remissions r
                JOIN technical_records tr ON tr.remission_id = r.id
                WHERE r.created_at BETWEEN :from AND :to
                """);
        qEquipos.setParameter("from", from);
        qEquipos.setParameter("to", to);

        Object[] eqRow = (Object[]) qEquipos.getSingleResult();
        long totalEquipos = eqRow[0] != null ? ((Number) eqRow[0]).longValue() : 0L;
        // BigDecimal totalValorEquipos = toBigDecimal(eqRow[1]); // disponible si lo necesitas luego

        // 1.3 Ingresos por remisiones (pendientes = abono, entregadas = total_value)
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

        // 1.4 Ticket promedio por remisión
        BigDecimal ticketPromedioRemision =
                totalRemisiones > 0
                        ? ingresosRemisiones.divide(BigDecimal.valueOf(totalRemisiones), 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

        // ==========================
        // 2) VENTAS
        // ==========================

        Query qVentas = em.createNativeQuery("""
                SELECT
                    COALESCE(SUM(s.sale_value), 0) AS total_ventas,
                    COUNT(*) AS total_transacciones,
                    COALESCE(SUM(s.unit_qty), 0) AS productos_totales
                FROM sales s
                WHERE s.sale_date BETWEEN :from AND :to
                """);
        qVentas.setParameter("from", from);
        qVentas.setParameter("to", to);

        Object[] vRow = (Object[]) qVentas.getSingleResult();
        BigDecimal totalVentas = toBigDecimal(vRow[0]);
        long totalTransacciones = vRow[1] != null ? ((Number) vRow[1]).longValue() : 0L;
        long productosTotales = vRow[2] != null ? ((Number) vRow[2]).longValue() : 0L;

        // Ticket promedio por venta (monto promedio por transacción)
        BigDecimal ticketPromedioVenta =
                totalTransacciones > 0
                        ? totalVentas.divide(BigDecimal.valueOf(totalTransacciones), 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

        // Unidades promedio por venta
        BigDecimal unidadesPromedioPorVenta =
                totalTransacciones > 0
                        ? BigDecimal.valueOf(productosTotales)
                        .divide(BigDecimal.valueOf(totalTransacciones), 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

        // Unidades promedio por remisión
        BigDecimal unidadesPromedioPorRemision =
                totalRemisiones > 0
                        ? BigDecimal.valueOf(productosTotales)
                        .divide(BigDecimal.valueOf(totalRemisiones), 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

        // ==========================
        // 3) GASTOS & GLOBAL
        // ==========================

        // 3.1 Total gastos usando ExpenseService
        BigDecimal totalGastos = expenseService.getTotalExpenses(from, to);

        // 3.2 Ingresos totales (remisiones + ventas)
        BigDecimal ingresosTotales = ingresosRemisiones.add(totalVentas);

        // 3.3 Ingreso neto
        BigDecimal ingresoNeto = ingresosTotales.subtract(totalGastos);

        // ==========================
        // 4) Construir DTOs
        // ==========================

        RemissionKpiDto remisionesDto = new RemissionKpiDto(
                totalRemisiones,
                totalEquipos,
                ingresosRemisiones,
                ticketPromedioRemision,
                unidadesPromedioPorRemision
        );

        SalesKpiDto ventasDto = new SalesKpiDto(
                totalTransacciones,
                productosTotales,
                totalVentas,
                ticketPromedioVenta,
                unidadesPromedioPorVenta
        );

        GlobalKpiDto globalDto = new GlobalKpiDto(
                ingresosTotales,
                totalGastos,
                ingresoNeto
        );

        return new FullReportDto(remisionesDto, ventasDto, globalDto);
    }

    // Helper seguro para convertir cualquier Number/BigDecimal a BigDecimal
    private BigDecimal toBigDecimal(Object raw) {
        if (raw == null) return BigDecimal.ZERO;
        if (raw instanceof BigDecimal bd) return bd;
        if (raw instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return new BigDecimal(raw.toString());
    }
}
