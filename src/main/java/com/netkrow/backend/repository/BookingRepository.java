package com.netkrow.backend.repository;

import com.netkrow.backend.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT COUNT(b) FROM Booking b " +
            "WHERE b.specialistId = :specialistId " +
            "  AND (b.startTime < :endTime AND b.endTime > :startTime)")
    long countOverlappingBookings(Long specialistId, LocalDateTime startTime, LocalDateTime endTime);

    // Método para obtener reservas dentro de un rango de fechas
    List<Booking> findAllByStartTimeBetween(LocalDateTime start, LocalDateTime end);
}
