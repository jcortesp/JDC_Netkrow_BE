package com.netkrow.backend.controller;

import com.netkrow.backend.model.Customer;
import com.netkrow.backend.repository.CustomerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerRepository repo;

    public CustomerController(CustomerRepository repo) { this.repo = repo; }

    @GetMapping
    public ResponseEntity<List<Customer>> list(@RequestParam(value = "q", required = false) String q) {
        if (q == null || q.isBlank()) {
            return ResponseEntity.ok(repo.findAll());
        }
        final String qq = q.trim().toLowerCase();
        List<Customer> all = repo.findAll();
        List<Customer> filtered = all.stream().filter(c -> {
            String first = c.getFirstName() != null ? c.getFirstName().toLowerCase() : "";
            String last  = c.getLastName() != null ? c.getLastName().toLowerCase() : "";
            String phone = c.getPhone() != null ? c.getPhone().toLowerCase() : "";
            String email = c.getEmail() != null ? c.getEmail().toLowerCase() : "";
            String doc   = c.getDocumentId() != null ? c.getDocumentId().toLowerCase() : "";
            String full  = (first + " " + last).trim();
            return first.contains(qq) || last.contains(qq) || full.contains(qq)
                    || phone.contains(qq) || email.contains(qq) || doc.contains(qq);
        }).toList();
        return ResponseEntity.ok(filtered);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getById(@PathVariable("id") Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public record DuplicateResponse(Long existingId, String field, String message) {}

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Customer c) {
        if (c.getFirstName() == null || c.getFirstName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "firstName es obligatorio"));
        }

        String email = c.getEmail() != null ? c.getEmail().trim() : null;
        String documentId = c.getDocumentId() != null ? c.getDocumentId().trim() : null;

        if (email != null && !email.isBlank()) {
            var existing = repo.findByEmailIgnoreCase(email);
            if (existing.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new DuplicateResponse(existing.get().getCustomerId(),
                                "email",
                                "Ya existe un cliente con ese email"));
            }
        }
        if (documentId != null && !documentId.isBlank()) {
            var existing = repo.findByDocumentIdIgnoreCase(documentId);
            if (existing.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new DuplicateResponse(existing.get().getCustomerId(),
                                "documentId",
                                "Ya existe un cliente con ese documento"));
            }
        }

        Customer saved = repo.save(c);
        return ResponseEntity.ok(saved);
    }
}
