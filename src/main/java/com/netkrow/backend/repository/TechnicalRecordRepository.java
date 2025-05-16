package com.netkrow.backend.repository;

import com.netkrow.backend.model.TechnicalRecord;
import com.netkrow.backend.model.Remission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TechnicalRecordRepository extends JpaRepository<TechnicalRecord, Long> {
    List<TechnicalRecord> findByRemission(Remission remission);
}


