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
        return repo.save(r);
    }

    public Remission save(Remission r) {
        return repo.save(r);
    }

    public java.util.Optional<Remission> getRemissionByRemissionId(String id) {
        return repo.findByRemissionId(id);
    }

    /**
     * Ingresar garantía: crea la remisión “-G” con todos los valores económicos en cero
     * y ambos métodos de pago fijados a "Garantia".
     */
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

        // Valores económicos en cero para la garantía
        g.setTotalValue(BigDecimal.ZERO);
        g.setDepositValue(BigDecimal.ZERO);
        g.setSaldo(BigDecimal.ZERO);

        // Métodos de pago de garantía
        g.setMetodoAbono("Garantia");
        g.setMetodoSaldo("Garantia");

        // Fecha de salida hasta que se cierre la garantía
        g.setFechaSalida(null);
        g.setGarantia(true);

        return repo.save(g);
    }

    /**
     * Cerrar garantía: marca fechaSalida. Los demás valores ya estaban en cero.
     */
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
