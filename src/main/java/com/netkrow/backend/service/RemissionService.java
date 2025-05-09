package com.netkrow.backend.service;

import com.netkrow.backend.model.Remission;
import com.netkrow.backend.repository.RemissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class RemissionService {

    @Autowired
    private RemissionRepository repo;

    public Remission createRemission(Remission r) {
        // Si es creación (id == null) y ya existe ese remissionId, lanzamos error
        if (r.getId() == null && repo.findByRemissionId(r.getRemissionId()).isPresent()) {
            throw new RuntimeException("Ya existe una remisión con ID " + r.getRemissionId());
        }
        return repo.save(r);
    }

    public Remission save(Remission r) {
        return repo.save(r);
    }

    public java.util.Optional<Remission> getRemissionByRemissionId(String id) {
        return repo.findByRemissionId(id);
    }

    public Remission createGarantia(String remissionId) {
        Remission orig = repo.findByRemissionId(remissionId)
                .orElseThrow(() -> new RuntimeException("Remisión original no encontrada"));

        String newId = remissionId + "-G";
        if (repo.findByRemissionId(newId).isPresent()) {
            throw new RuntimeException("Ya existe garantía para esta remisión");
        }

        Remission g = new Remission();
        g.setRemissionId(newId);
        g.setCreatedAt(LocalDateTime.now());
        g.setTotalValue(BigDecimal.ZERO);
        g.setDepositValue(BigDecimal.ZERO);
        g.setSaldo(BigDecimal.ZERO);
        g.setMetodoAbono("Garantia");
        g.setMetodoSaldo("Garantia");
        g.setFechaSalida(null);
        g.setGarantia(true);

        return repo.save(g);
    }

    public Remission closeGarantia(String remissionId) {
        Remission g = repo.findByRemissionId(remissionId)
                .orElseThrow(() -> new RuntimeException("Garantía no encontrada"));

        if (!Boolean.TRUE.equals(g.isGarantia())) {
            throw new RuntimeException("No es una remisión de garantía");
        }
        if (g.getFechaSalida() != null) {
            throw new RuntimeException("La garantía ya está cerrada");
        }

        g.setFechaSalida(LocalDateTime.now());
        return repo.save(g);
    }
}
