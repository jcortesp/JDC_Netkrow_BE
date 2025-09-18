package com.netkrow.backend.service;

import com.netkrow.backend.model.TechnicalRecord;
import com.netkrow.backend.model.Remission;
import com.netkrow.backend.repository.TechnicalRecordRepository;
import com.netkrow.backend.repository.RemissionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TechnicalRecordService {

    private final TechnicalRecordRepository repo;
    private final RemissionService remissionService;
    private final RemissionRepository remissionRepository;

    public TechnicalRecordService(
            TechnicalRecordRepository repo,
            RemissionService remissionService,
            RemissionRepository remissionRepository
    ) {
        this.repo = repo;
        this.remissionService = remissionService;
        this.remissionRepository = remissionRepository;
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
        if (data.getValor() == null || data.getValor() < 0) data.setValor(0.0);
        if (data.getDadoBaja() == null) data.setDadoBaja(false);
        if (Boolean.TRUE.equals(data.getDadoBaja())) {
            data.setFechaBaja(LocalDateTime.now());
        }
        TechnicalRecord saved = repo.save(data);
        recalculateRemissionTotal(rem);
        return saved;
    }

    @Transactional
    public TechnicalRecord update(String remissionCode, Long recordId, TechnicalRecord data) {
        Remission rem = remissionService.findByRemissionId(remissionCode);
        TechnicalRecord existing = repo.findById(recordId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro no encontrado: " + recordId));

        if (!existing.getRemission().getId().equals(rem.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El registro no pertenece a la remisión indicada");
        }

        existing.setEquipo(data.getEquipo());
        existing.setValor(data.getValor() == null || data.getValor() < 0 ? 0.0 : data.getValor());
        existing.setMarca(data.getMarca());
        existing.setSerial(data.getSerial());
        existing.setBrazalete(data.getBrazalete());
        existing.setPilas(data.getPilas());
        existing.setRevision(data.getRevision());
        existing.setMantenimiento(data.getMantenimiento());
        existing.setLimpieza(data.getLimpieza());
        existing.setCalibracion(data.getCalibracion());
        existing.setNotasDiagnostico(data.getNotasDiagnostico());

        TechnicalRecord saved = repo.save(existing);
        recalculateRemissionTotal(rem);
        return saved;
    }

    @Transactional
    public TechnicalRecord dropRecord(String remissionCode, Long recordId, boolean cobrarRevision, Double revisionValue) {
        Remission rem = remissionService.findByRemissionId(remissionCode);
        TechnicalRecord tr = repo.findById(recordId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro no encontrado: " + recordId));

        if (!tr.getRemission().getId().equals(rem.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El registro no pertenece a la remisión indicada");
        }
        if (Boolean.TRUE.equals(tr.getDadoBaja())) {
            // Idempotente: ya está dado de baja; solo recalculamos por si acaso
            recalculateRemissionTotal(rem);
            return tr;
        }

        tr.setDadoBaja(true);
        tr.setFechaBaja(LocalDateTime.now());

        if (cobrarRevision) {
            if (revisionValue == null || revisionValue < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valor de revisión inválido");
            }
            tr.setRevisionValor(revisionValue);
        } else {
            tr.setRevisionValor(0.0);
        }

        TechnicalRecord saved = repo.save(tr);
        recalculateRemissionTotal(rem);
        return saved;
    }

    private void recalculateRemissionTotal(Remission rem) {
        Double activos = repo.sumActivosValor(rem);
        Double bajas   = repo.sumBajasRevision(rem);
        double total = (activos == null ? 0.0 : activos) + (bajas == null ? 0.0 : bajas);
        rem.setTotalValue(total);
        remissionRepository.save(rem);
    }
}
