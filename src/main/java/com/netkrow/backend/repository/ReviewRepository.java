package com.netkrow.backend.repository;

import com.netkrow.backend.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Permite verificar si existe una reseña para una reserva dada
    Optional<Review> findByBooking_Id(Long bookingId);
}
