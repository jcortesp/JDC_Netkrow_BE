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

    @GetMapping
    public ResponseEntity<List<TechnicalRecord>> list(@PathVariable String remissionId) {
        return ResponseEntity.ok(service.listByRemissionCode(remissionId));
    }

    @PostMapping
    public ResponseEntity<TechnicalRecord> create(
            @PathVariable String remissionId,
            @RequestBody TechnicalRecord record
    ) {
        TechnicalRecord saved = service.create(remissionId, record);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{recordId}")
    public ResponseEntity<TechnicalRecord> update(
            @PathVariable String remissionId,
            @PathVariable Long recordId,
            @RequestBody TechnicalRecord record
    ) {
        TechnicalRecord updated = service.update(remissionId, recordId, record);
        return ResponseEntity.ok(updated);
    }
}
