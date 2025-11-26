package com.netkrow.backend.controller;

import com.netkrow.backend.model.Expense;
import com.netkrow.backend.service.ExpenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService service;

    public ExpenseController(ExpenseService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Expense> create(@RequestBody Expense expense) {
        Expense saved = service.create(expense);
        return ResponseEntity.ok(saved);
    }
}
