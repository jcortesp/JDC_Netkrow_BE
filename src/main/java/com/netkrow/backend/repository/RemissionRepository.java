package com.netkrow.backend.repository;

import com.netkrow.backend.model.Remission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RemissionRepository extends JpaRepository<Remission, Long> {
    Optional<Remission> findByRemissionId(String remissionId);
}
