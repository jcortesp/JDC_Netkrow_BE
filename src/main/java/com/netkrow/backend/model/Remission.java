package com.netkrow.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "remissions")
public class Remission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String remissionId;

    private BigDecimal totalValue;
    private BigDecimal depositValue;
    private BigDecimal saldo;

    private String metodoAbono;
    private String metodoSaldo;
    private LocalDateTime fechaSalida;

    // — Campos de Servicio Técnico —
    private String equipo;
    private String marca;
    private String serial;
    private String brazalete;
    private String pilas;
    private String revision;
    private String mantenimiento;
    private String limpieza;
    private String calibracion;

    @Column(name = "notas_diagnostico", length = 1000)
    private String notasDiagnostico;

    private LocalDateTime createdAt;

    /** NUEVO: flag de garantía */
    @Column(nullable = false)
    private boolean garantia = false;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (totalValue != null && depositValue != null) {
            this.saldo = totalValue.subtract(depositValue);
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (totalValue != null && depositValue != null) {
            this.saldo = totalValue.subtract(depositValue);
        }
    }

    // — Getters y setters —

    public Long getId() { return id; }

    public String getRemissionId() { return remissionId; }
    public void setRemissionId(String remissionId) { this.remissionId = remissionId; }

    public BigDecimal getTotalValue() { return totalValue; }
    public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }

    public BigDecimal getDepositValue() { return depositValue; }
    public void setDepositValue(BigDecimal depositValue) { this.depositValue = depositValue; }

    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }

    public String getMetodoAbono() { return metodoAbono; }
    public void setMetodoAbono(String metodoAbono) { this.metodoAbono = metodoAbono; }

    public String getMetodoSaldo() { return metodoSaldo; }
    public void setMetodoSaldo(String metodoSaldo) { this.metodoSaldo = metodoSaldo; }

    public LocalDateTime getFechaSalida() { return fechaSalida; }
    public void setFechaSalida(LocalDateTime fechaSalida) { this.fechaSalida = fechaSalida; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getEquipo() { return equipo; }
    public void setEquipo(String equipo) { this.equipo = equipo; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getSerial() { return serial; }
    public void setSerial(String serial) { this.serial = serial; }

    public String getBrazalete() { return brazalete; }
    public void setBrazalete(String brazalete) { this.brazalete = brazalete; }

    public String getPilas() { return pilas; }
    public void setPilas(String pilas) { this.pilas = pilas; }

    public String getRevision() { return revision; }
    public void setRevision(String revision) { this.revision = revision; }

    public String getMantenimiento() { return mantenimiento; }
    public void setMantenimiento(String mantenimiento) { this.mantenimiento = mantenimiento; }

    public String getLimpieza() { return limpieza; }
    public void setLimpieza(String limpieza) { this.limpieza = limpieza; }

    public String getCalibracion() { return calibracion; }
    public void setCalibracion(String calibracion) { this.calibracion = calibracion; }

    public String getNotasDiagnostico() { return notasDiagnostico; }
    public void setNotasDiagnostico(String notasDiagnostico) { this.notasDiagnostico = notasDiagnostico; }

    // — Getters/setters garantía —

    public boolean isGarantia() { return garantia; }
    public void setGarantia(boolean garantia) { this.garantia = garantia; }
}
