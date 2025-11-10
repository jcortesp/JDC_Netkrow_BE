package com.netkrow.backend.controller;

import com.netkrow.backend.dto.SaleDTO;
import com.netkrow.backend.service.SaleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // Para pruebas en local: listar ventas creadas
    @GetMapping
    public ResponseEntity<List<SaleDTO>> list() {
        return ResponseEntity.ok(service.list());
    }
}
