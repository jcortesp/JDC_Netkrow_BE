package com.netkrow.backend.repository;

import com.netkrow.backend.model.RCARecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RCARecordRepository extends JpaRepository<RCARecord, Long> {
    List<RCARecord> findByErrorCodeContainingIgnoreCaseOrOmsComponentContainingIgnoreCaseOrTagsContainingIgnoreCase(
            String errorCode, String omsComponent, String tags
    );
}
