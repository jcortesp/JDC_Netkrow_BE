package com.netkrow.backend.controller;

import com.netkrow.backend.dto.SaleDTO;
import com.netkrow.backend.service.SaleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

    private final SaleService service;
    public SaleController(SaleService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<SaleDTO> create(@RequestBody SaleDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    // Listado con filtros básicos (opcionales)
    @GetMapping
    public ResponseEntity<List<SaleDTO>> list(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "dateFrom", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(value = "dateTo", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(value = "channel", required = false) String channel,
            @RequestParam(value = "paymentMethod", required = false) String paymentMethod,
            @RequestParam(value = "transactionType", required = false) String transactionType,
            @RequestParam(value = "customerId", required = false) Long customerId
    ) {
        return ResponseEntity.ok(
                service.listFiltered(q, dateFrom, dateTo, channel, paymentMethod, transactionType, customerId)
        );
    }
}
