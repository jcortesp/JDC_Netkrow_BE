package com.netkrow.backend.controller;

import com.netkrow.backend.model.Remission;
import com.netkrow.backend.service.RemissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/remissions")
public class RemissionController {

    private final RemissionService service;

    public RemissionController(RemissionService service) {
        this.service = service;
    }

    // DTO para entrega normal
    public static class DeliveryRequest {
        private String metodoSaldo;
        public String getMetodoSaldo() { return metodoSaldo; }
        public void setMetodoSaldo(String metodoSaldo) { this.metodoSaldo = metodoSaldo; }
    }

    // DTO para dar de baja
    public static class DropRequest {
        private boolean cobrarRevision;
        private Double revisionValue;
        public boolean isCobrarRevision() { return cobrarRevision; }
        public void setCobrarRevision(boolean cobrarRevision) { this.cobrarRevision = cobrarRevision; }
        public Double getRevisionValue() { return revisionValue; }
        public void setRevisionValue(Double revisionValue) { this.revisionValue = revisionValue; }
    }

    @PostMapping
    public ResponseEntity<Remission> create(@RequestBody Remission r) {
        Remission created = service.create(r);
        return ResponseEntity
                .created(URI.create("/api/remissions/" + created.getRemissionId()))
                .body(created);
    }

    @GetMapping
    public ResponseEntity<List<Remission>> list() {
        return ResponseEntity.ok(service.listAll());
    }

    @GetMapping("/{remissionId}")
    public ResponseEntity<Remission> getOne(@PathVariable String remissionId) {
        return ResponseEntity.ok(service.findByRemissionId(remissionId));
    }

    @PutMapping("/deliver/{remissionId}")
    public ResponseEntity<Remission> deliver(
            @PathVariable String remissionId,
            @RequestBody DeliveryRequest req
    ) {
        return ResponseEntity.ok(
                service.deliver(remissionId, req.getMetodoSaldo())
        );
    }

    @PutMapping("/{remissionId}/dar-baja")
    public ResponseEntity<Remission> dropRemission(
            @PathVariable String remissionId,
            @RequestBody DropRequest req
    ) {
        return ResponseEntity.ok(
                service.drop(remissionId, req.isCobrarRevision(), req.getRevisionValue())
        );
    }

    @PutMapping("/{remissionId}/garantia")
    public ResponseEntity<Remission> createGarantia(@PathVariable String remissionId) {
        return ResponseEntity.ok(service.createGarantia(remissionId));
    }

    @PutMapping("/{remissionId}/garantia/sacar")
    public ResponseEntity<Remission> closeGarantia(@PathVariable String remissionId) {
        return ResponseEntity.ok(service.closeGarantia(remissionId));
    }
}
