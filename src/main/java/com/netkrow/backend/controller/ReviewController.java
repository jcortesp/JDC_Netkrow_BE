package com.netkrow.backend.controller;

import com.netkrow.backend.model.Review;
import com.netkrow.backend.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    // DTO para crear la reseña
    public static class ReviewRequest {
        public Long bookingId;
        public Long clientId;
        public int rating;
        public String comment;
        // Getters & setters (puedes generarlos o usar propiedades públicas según prefieras)
    }

    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody ReviewRequest request) {
        // Validación mínima del rating
        if (request.rating < 1 || request.rating > 5) {
            return ResponseEntity.badRequest().body("Rating debe ser entre 1 y 5");
        }
        try {
            Review review = reviewService.createReview(
                    request.bookingId,
                    request.clientId,
                    request.rating,
                    request.comment
            );
            return ResponseEntity.ok(review);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<?> getReview(@PathVariable Long reviewId) {
        return reviewService.getReview(reviewId)
                .map(r -> ResponseEntity.ok(r))
                .orElse(ResponseEntity.notFound().build());
    }
}
