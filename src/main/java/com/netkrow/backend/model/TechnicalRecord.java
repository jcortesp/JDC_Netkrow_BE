package com.netkrow.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "technical_records")
public class TechnicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "remission_id", nullable = false)
    private Remission remission;

    @Column(nullable = false)
    private String equipo;

    @Column
    private String marca;

    @Column
    private String serial;

    @Column
    private String brazalete;

    @Column
    private String pilas;

    @Column
    private String revision;

    @Column
    private String mantenimiento;

    @Column
    private String limpieza;

    @Column
    private String calibracion;

    @Column(name = "notas_diagnostico", length = 100)
    private String notasDiagnostico;

    // ===== Campos monetarios / baja por equipo =====
    @Column(name = "valor", nullable = false)
    private Double valor = 0.0;

    @Column(name = "dado_baja", nullable = false)
    private Boolean dadoBaja = false;

    @Column(name = "fecha_baja")
    private LocalDateTime fechaBaja;

    @Column(name = "revision_valor")
    private Double revisionValor; // null si no se cobró revisión

    // Getters & Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public Remission getRemission() { return remission; }
    public void setRemission(Remission remission) { this.remission = remission; }

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

    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }

    public Boolean getDadoBaja() { return dadoBaja; }
    public void setDadoBaja(Boolean dadoBaja) { this.dadoBaja = dadoBaja; }

    public LocalDateTime getFechaBaja() { return fechaBaja; }
    public void setFechaBaja(LocalDateTime fechaBaja) { this.fechaBaja = fechaBaja; }

    public Double getRevisionValor() { return revisionValor; }
    public void setRevisionValor(Double revisionValor) { this.revisionValor = revisionValor; }
}
