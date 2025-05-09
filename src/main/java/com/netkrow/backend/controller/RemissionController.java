// src/main/java/com/netkrow/backend/controller/RemissionController.java
package com.netkrow.backend.controller;

import com.netkrow.backend.model.Remission;
import com.netkrow.backend.service.RemissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://netkrow-fe.vercel.app",
        "https://netkrow.onrender.com"
})
@RestController
@RequestMapping("/api/remissions")
public class RemissionController {

    @Autowired
    private RemissionService service;

    // DTO para creación de remisión
    public static class RemissionRequest {
        private String remissionId;
        private BigDecimal totalValue;
        private BigDecimal depositValue;
        private String depositMethod;
        public String getRemissionId() { return remissionId; }
        public void setRemissionId(String remissionId) { this.remissionId = remissionId; }
        public BigDecimal getTotalValue() { return totalValue; }
        public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }
        public BigDecimal getDepositValue() { return depositValue; }
        public void setDepositValue(BigDecimal depositValue) { this.depositValue = depositValue; }
        public String getDepositMethod() { return depositMethod; }
        public void setDepositMethod(String depositMethod) { this.depositMethod = depositMethod; }
    }

    // DTO para entrega de equipo
    public static class DeliveryRequest {
        private String metodoSaldo;
        public String getMetodoSaldo() { return metodoSaldo; }
        public void setMetodoSaldo(String metodoSaldo) { this.metodoSaldo = metodoSaldo; }
    }

    // DTO para actualización técnica
    public static class TechnicalRequest {
        private String equipo;
        private String marca;
        private String serial;
        private String brazalete;
        private String pilas;
        private String revision;
        private String mantenimiento;
        private String limpieza;
        private String calibracion;
        private String notasDiagnostico;
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
    }

    // 1) Crear nueva remisión, evitando duplicados
    @PostMapping
    public ResponseEntity<?> createRemission(@RequestBody RemissionRequest req) {
        if (service.getRemissionByRemissionId(req.getRemissionId()).isPresent()) {
            return ResponseEntity.badRequest().body("Ya existe una remisión con este ID");
        }
        Remission r = new Remission();
        r.setRemissionId(req.getRemissionId());
        r.setTotalValue(req.getTotalValue());
        r.setDepositValue(req.getDepositValue());
        r.setMetodoAbono(req.getDepositMethod());
        return ResponseEntity.ok(service.createRemission(r));
    }

    // 2) Registrar entrega de equipo
    @PutMapping("/deliver/{remissionId}")
    public ResponseEntity<?> deliverEquipment(
            @PathVariable String remissionId,
            @RequestBody DeliveryRequest req) {

        Optional<Remission> opt = service.getRemissionByRemissionId(remissionId);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body("Remisión no encontrada");
        }
        Remission r = opt.get();
        r.setMetodoSaldo(req.getMetodoSaldo());
        r.setFechaSalida(LocalDateTime.now());
        return ResponseEntity.ok(service.createRemission(r));
    }

    // 3) Dar de baja
    @PutMapping("/{remissionId}/dar-baja")
    public ResponseEntity<?> dropRemission(@PathVariable String remissionId) {
        try {
            return ResponseEntity.ok(service.dropRemission(remissionId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4) Actualizar datos técnicos
    @PutMapping("/{remissionId}/technical")
    public ResponseEntity<?> updateTechnical(
            @PathVariable String remissionId,
            @RequestBody TechnicalRequest req) {

        Optional<Remission> opt = service.getRemissionByRemissionId(remissionId);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body("Remisión no encontrada");
        }
        Remission r = opt.get();
        r.setEquipo(req.getEquipo());
        r.setMarca(req.getMarca());
        r.setSerial(req.getSerial());
        r.setBrazalete(req.getBrazalete());
        r.setPilas(req.getPilas());
        r.setRevision(req.getRevision());
        r.setMantenimiento(req.getMantenimiento());
        r.setLimpieza(req.getLimpieza());
        r.setCalibracion(req.getCalibracion());
        r.setNotasDiagnostico(req.getNotasDiagnostico());
        return ResponseEntity.ok(service.createRemission(r));
    }

    // 5) Obtener remisión por ID
    @GetMapping("/{remissionId}")
    public ResponseEntity<?> getRemission(@PathVariable String remissionId) {
        Optional<Remission> opt = service.getRemissionByRemissionId(remissionId);
        if (opt.isPresent()) {
            return ResponseEntity.ok(opt.get());
        } else {
            return ResponseEntity.status(404).body("Remisión no encontrada");
        }
    }

    // 6) Crear garantía
    @PutMapping("/{remissionId}/garantia")
    public ResponseEntity<?> createGarantia(@PathVariable String remissionId) {
        try {
            return ResponseEntity.ok(service.createGarantia(remissionId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 7) Cerrar garantía
    @PutMapping("/{remissionId}/garantia/sacar")
    public ResponseEntity<?> closeGarantia(@PathVariable String remissionId) {
        try {
            return ResponseEntity.ok(service.closeGarantia(remissionId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
