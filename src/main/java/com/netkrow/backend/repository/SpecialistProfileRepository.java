package com.netkrow.backend.repository;

import com.netkrow.backend.model.SpecialistProfile;
import com.netkrow.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpecialistProfileRepository extends JpaRepository<SpecialistProfile, Long> {

    @Query("""
        SELECT sp FROM SpecialistProfile sp
        WHERE 
          (:skill IS NULL OR :skill = '' OR :skill MEMBER OF sp.skills)
          AND (:minRate IS NULL OR sp.ratePerHour >= :minRate)
          AND (:maxRate IS NULL OR sp.ratePerHour <= :maxRate)
        """)
    List<SpecialistProfile> search(
            @Param("skill") String skill,
            @Param("minRate") BigDecimal minRate,
            @Param("maxRate") BigDecimal maxRate
    );

    // Buscar el perfil por usuario
    Optional<SpecialistProfile> findByUser(User user);
}
