package com.netkrow.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Referencia a la reserva. De esta forma sabemos qué booking se está reseñando.
    @OneToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    // El cliente que deja la reseña
    @ManyToOne
    @JoinColumn(name = "client_id")
    private User client;

    private int rating;      // Por ejemplo, de 1 a 5
    private String comment;  // Comentario o feedback

    private LocalDateTime createdAt;  // Cuándo se creó la reseña

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public User getClient() { return client; }
    public void setClient(User client) { this.client = client; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
