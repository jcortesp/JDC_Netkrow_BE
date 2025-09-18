package com.netkrow.backend.repository;

import com.netkrow.backend.model.TechnicalRecord;
import com.netkrow.backend.model.Remission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TechnicalRecordRepository extends JpaRepository<TechnicalRecord, Long> {
    List<TechnicalRecord> findByRemission(Remission remission);

    @Query("select coalesce(sum(tr.valor), 0) from TechnicalRecord tr where tr.remission = :rem and tr.dadoBaja = false")
    Double sumActivosValor(Remission rem);

    @Query("select coalesce(sum(tr.revisionValor), 0) from TechnicalRecord tr where tr.remission = :rem and tr.dadoBaja = true")
    Double sumBajasRevision(Remission rem);
}
