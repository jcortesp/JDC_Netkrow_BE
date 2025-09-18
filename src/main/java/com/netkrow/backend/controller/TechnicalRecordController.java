package com.netkrow.backend.controller;

import com.netkrow.backend.model.TechnicalRecord;
import com.netkrow.backend.service.TechnicalRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/remissions/{remissionId}/technical-records")
public class TechnicalRecordController {

    private final TechnicalRecordService service;

    public TechnicalRecordController(TechnicalRecordService service) {
        this.service = service;
    }

    public static class DropRecordRequest {
        private boolean cobrarRevision;
        private Double revisionValue;
        public boolean isCobrarRevision() { return cobrarRevision; }
        public void setCobrarRevision(boolean cobrarRevision) { this.cobrarRevision = cobrarRevision; }
        public Double getRevisionValue() { return revisionValue; }
        public void setRevisionValue(Double revisionValue) { this.revisionValue = revisionValue; }
    }

    @GetMapping
    public ResponseEntity<List<TechnicalRecord>> list(@PathVariable String remissionId) {
        return ResponseEntity.ok(service.listByRemissionCode(remissionId));
    }

    @PostMapping
    public ResponseEntity<TechnicalRecord> create(
            @PathVariable String remissionId,
            @RequestBody TechnicalRecord record
    ) {
        return ResponseEntity.ok(service.create(remissionId, record));
    }

    @PutMapping("/{recordId}")
    public ResponseEntity<TechnicalRecord> update(
            @PathVariable String remissionId,
            @PathVariable Long recordId,
            @RequestBody TechnicalRecord record
    ) {
        return ResponseEntity.ok(service.update(remissionId, recordId, record));
    }

    @PutMapping("/{recordId}/drop")
    public ResponseEntity<TechnicalRecord> dropRecord(
            @PathVariable String remissionId,
            @PathVariable Long recordId,
            @RequestBody DropRecordRequest req
    ) {
        TechnicalRecord updated = service.dropRecord(
                remissionId,
                recordId,
                req.isCobrarRevision(),
                req.getRevisionValue()
        );
        return ResponseEntity.ok(updated);
    }
}
