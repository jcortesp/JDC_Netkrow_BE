package com.netkrow.backend.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "remissions")
public class Remission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "remission_id", unique = true, nullable = false)
    private String remissionId;

    @Column(name = "total_value", nullable = false)
    private Double totalValue;

    @Column(name = "deposit_value", nullable = false)
    private Double depositValue;

    @JsonAlias("depositMethod")
    @Column(name = "metodo_abono")
    private String metodoAbono;

    @Column(name = "metodo_saldo")
    private String metodoSaldo;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "fecha_salida")
    private LocalDateTime fechaSalida;

    @Column(name = "saldo", nullable = false)
    private Double saldo;

    @Column(nullable = false)
    private boolean garantia = false;

    // —— Getters & setters ——

    public Long getId() { return id; }

    public String getRemissionId() { return remissionId; }
    public void setRemissionId(String remissionId) { this.remissionId = remissionId; }

    public Double getTotalValue() { return totalValue; }
    public void setTotalValue(Double totalValue) { this.totalValue = totalValue; }

    public Double getDepositValue() { return depositValue; }
    public void setDepositValue(Double depositValue) { this.depositValue = depositValue; }

    public String getMetodoAbono() { return metodoAbono; }
    public void setMetodoAbono(String metodoAbono) { this.metodoAbono = metodoAbono; }

    public String getMetodoSaldo() { return metodoSaldo; }
    public void setMetodoSaldo(String metodoSaldo) { this.metodoSaldo = metodoSaldo; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getFechaSalida() { return fechaSalida; }
    public void setFechaSalida(LocalDateTime fechaSalida) { this.fechaSalida = fechaSalida; }

    public Double getSaldo() { return saldo; }

    public boolean isGarantia() { return garantia; }
    public void setGarantia(boolean garantia) { this.garantia = garantia; }

    // —— Lifecycle hooks para calcular saldo automáticamente ——

    @PrePersist
    public void onCreate() {
        calculateSaldo();
    }

    @PreUpdate
    public void onUpdate() {
        calculateSaldo();
    }

    private void calculateSaldo() {
        double tot = (this.totalValue != null ? this.totalValue : 0.0);
        double dep = (this.depositValue != null ? this.depositValue : 0.0);
        this.saldo = tot - dep;
    }
}
