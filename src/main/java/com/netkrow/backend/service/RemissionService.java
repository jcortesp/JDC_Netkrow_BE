package com.netkrow.backend.service;

import com.netkrow.backend.model.Remission;
import com.netkrow.backend.repository.RemissionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RemissionService {

    private final RemissionRepository repo;

    public RemissionService(RemissionRepository repo) {
        this.repo = repo;
    }

    // --- Idempotencia en memoria (TTL 10 min) ---
    private static final Duration IDEM_TTL = Duration.ofMinutes(10);
    private static final Map<String, CacheEntry> IDEM_CACHE = new ConcurrentHashMap<>();

    private static class CacheEntry {
        final Remission remission;
        final LocalDateTime createdAt;
        CacheEntry(Remission r) {
            this.remission = r;
            this.createdAt = LocalDateTime.now();
        }
        boolean expired() {
            return createdAt.plus(IDEM_TTL).isBefore(LocalDateTime.now());
        }
    }

    private static void cachePut(String key, Remission r) {
        if (key == null || key.isBlank() || r == null) return;
        IDEM_CACHE.put(key, new CacheEntry(r));
        // Limpieza oportunista
        IDEM_CACHE.entrySet().removeIf(e -> e.getValue().expired());
    }

    private static Optional<Remission> cacheGet(String key) {
        if (key == null || key.isBlank()) return Optional.empty();
        CacheEntry ce = IDEM_CACHE.get(key);
        if (ce == null) return Optional.empty();
        if (ce.expired()) {
            IDEM_CACHE.remove(key);
            return Optional.empty();
        }
        return Optional.of(ce.remission);
    }

    // --- Lógica existente + idempotencia ---

    @Transactional
    public Remission create(Remission r) {
        if (repo.findByRemissionId(r.getRemissionId()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una remisión con ID: " + r.getRemissionId()
            );
        }
        return repo.save(r);
    }

    /**
     * Crea con idempotencia: si llega la misma petición con el mismo header
     * Idempotency-Key, devuelve la misma remisión sin duplicar en DB.
     */
    @Transactional
    public Remission createWithIdempotency(Remission r, String idemKey) {
        Optional<Remission> cached = cacheGet(idemKey);
        if (cached.isPresent()) {
            return cached.get();
        }

        Optional<Remission> existing = repo.findByRemissionId(r.getRemissionId());
        if (existing.isPresent()) {
            Remission ex = existing.get();
            cachePut(idemKey, ex);
            return ex;
        }

        Remission created = repo.save(r);
        cachePut(idemKey, created);
        return created;
    }

    @Transactional(readOnly = true)
    public List<Remission> listAll() {
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public Remission findByRemissionId(String id) {
        return repo.findByRemissionId(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Remisión no encontrada: " + id)
                );
    }

    @Transactional
    public Remission deliver(String remissionId, String metodoSaldo) {
        Remission r = findByRemissionId(remissionId);
        if (r.getFechaSalida() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya fue entregada");
        }

        // saldo = totalValue - depositValue
        double total = r.getTotalValue() == null ? 0.0 : r.getTotalValue();
        double abono = r.getDepositValue() == null ? 0.0 : r.getDepositValue();
        double saldo = total - abono;

        if (saldo > 0.00001 && (metodoSaldo == null || metodoSaldo.isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debe seleccionar método de pago para el saldo pendiente.");
        }

        r.setFechaSalida(LocalDateTime.now());
        r.setMetodoSaldo(metodoSaldo);
        return repo.save(r);
    }

    @Transactional
    public Remission drop(String remissionId, boolean cobrarRevision, Double revisionValue) {
        Remission r = findByRemissionId(remissionId);
        if (r.getFechaSalida() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La remisión ya fue dada de baja o entregada");
        }
        r.setFechaSalida(LocalDateTime.now());
        r.setMetodoSaldo("Baja");

        if (cobrarRevision) {
            if (revisionValue == null || revisionValue < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Valor de revisión inválido");
            }
            r.setTotalValue(revisionValue);
            r.setDepositValue(revisionValue);
        } else {
            r.setTotalValue(0.0);
            r.setDepositValue(0.0);
            r.setMetodoAbono("Baja");
        }

        return repo.save(r);
    }

    @Transactional
    public Remission createGarantia(String remissionId) {
        findByRemissionId(remissionId);
        String garantiaId = remissionId + "-G";
        if (repo.findByRemissionId(garantiaId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe garantía para esta remisión");
        }
        Remission g = new Remission();
        g.setRemissionId(garantiaId);
        g.setTotalValue(0.0);
        g.setDepositValue(0.0);
        g.setMetodoAbono("Garantía");
        g.setMetodoSaldo("Garantía");
        g.setGarantia(true);
        return repo.save(g);
    }

    @Transactional
    public Remission closeGarantia(String garantiaId) {
        Remission g = findByRemissionId(garantiaId);
        if (!g.isGarantia()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No es una remisión de garantía");
        }
        if (g.getFechaSalida() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La garantía ya está cerrada");
        }
        g.setFechaSalida(LocalDateTime.now());
        return repo.save(g);
    }
}
