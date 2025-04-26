package com.netkrow.backend.service;

import com.netkrow.backend.model.Booking;
import com.netkrow.backend.model.Review;
import com.netkrow.backend.model.User;
import com.netkrow.backend.repository.ReviewRepository;
import com.netkrow.backend.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserService userService;

    /**
     * Crea una reseña para una reserva.
     *
     * @param bookingId El identificador de la reserva.
     * @param clientId  El identificador del cliente que deja la reseña.
     * @param rating    La puntuación (por ejemplo, de 1 a 5).
     * @param comment   El comentario o feedback.
     * @return La reseña creada.
     * @throws IllegalStateException si no se encuentra la reserva, el cliente, o
     *                               si el cliente no corresponde con la reserva, o si ya existe una reseña para esa reserva.
     */
    public Review createReview(Long bookingId, Long clientId, int rating, String comment) {
        // Buscar la reserva mediante su ID.
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking no encontrado"));

        // Verificar que la reserva aún no tenga una reseña asociada.
        if (reviewRepository.findByBooking_Id(bookingId).isPresent()) {
            throw new IllegalStateException("Ya existe una reseña para esta reserva");
        }

        // Buscar el cliente mediante su ID.
        User client = userService.findById(clientId)
                .orElseThrow(() -> new IllegalStateException("Cliente no encontrado"));

        // Verificar que el cliente indicado sea el mismo asociado a la reserva.
        // Dado que en Booking sólo tenemos el ID del cliente, se compara:
        if (!clientId.equals(booking.getClientId())) {
            throw new IllegalStateException("El cliente no está asociado a esta reserva");
        }

        // Crear la reseña y asignar sus propiedades
        Review review = new Review();
        review.setBooking(booking);
        review.setClient(client);
        review.setRating(rating);
        review.setComment(comment);
        review.setCreatedAt(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    public Optional<Review> getReview(Long reviewId) {
        return reviewRepository.findById(reviewId);
    }
}
