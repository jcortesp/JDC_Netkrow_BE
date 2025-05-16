package com.netkrow.backend.service;

import com.netkrow.backend.model.TechnicalRecord;
import com.netkrow.backend.model.Remission;
import com.netkrow.backend.repository.TechnicalRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TechnicalRecordService {

    private final TechnicalRecordRepository repo;
    private final RemissionService remissionService;

    public TechnicalRecordService(
            TechnicalRecordRepository repo,
            RemissionService remissionService
    ) {
        this.repo = repo;
        this.remissionService = remissionService;
    }

    @Transactional(readOnly = true)
    public List<TechnicalRecord> listByRemissionCode(String remissionCode) {
        Remission rem = remissionService.findByRemissionId(remissionCode);
        return repo.findByRemission(rem);
    }

    @Transactional
    public TechnicalRecord create(String remissionCode, TechnicalRecord data) {
        Remission rem = remissionService.findByRemissionId(remissionCode);
        data.setRemission(rem);
        return repo.save(data);
    }

    @Transactional
    public TechnicalRecord update(String remissionCode, Long recordId, TechnicalRecord data) {
        Remission rem = remissionService.findByRemissionId(remissionCode);
        TechnicalRecord existing = repo.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Registro no encontrado: " + recordId));

        if (!existing.getRemission().getId().equals(rem.getId())) {
            throw new RuntimeException("El registro no pertenece a la remisión indicada");
        }

        // Sólo copiamos los campos editables
        existing.setEquipo(data.getEquipo());
        existing.setMarca(data.getMarca());
        existing.setSerial(data.getSerial());
        existing.setBrazalete(data.getBrazalete());
        existing.setPilas(data.getPilas());
        existing.setRevision(data.getRevision());
        existing.setMantenimiento(data.getMantenimiento());
        existing.setLimpieza(data.getLimpieza());
        existing.setCalibracion(data.getCalibracion());
        existing.setNotasDiagnostico(data.getNotasDiagnostico());

        return repo.save(existing);
    }
}
