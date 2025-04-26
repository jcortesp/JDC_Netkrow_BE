package com.netkrow.backend.controller;

import com.netkrow.backend.model.Review;
import com.netkrow.backend.security.JwtUtils;
import com.netkrow.backend.service.ReviewService;
import com.netkrow.backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
public class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    // Bean simulado para UserService, necesario para el filtro de seguridad o dependencias internas
    @MockBean
    private UserService userService;

    // Bean simulado para JwtUtils, requerido por el filtro de seguridad
    @MockBean
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    // Prueba exitosa de creación de reseña con datos válidos
    @Test
    @WithMockUser(roles = "CLIENT")
    public void testCreateReviewValid() throws Exception {
        Long bookingId = 101L;
        Long clientId = 1L;
        int rating = 5;
        String comment = "Excelente servicio";

        Review review = new Review();
        review.setId(10L);
        review.setRating(rating);
        review.setComment(comment);
        // Se omiten otras propiedades para simplificar

        // Simulamos que el servicio retorna la reseña creada
        Mockito.when(reviewService.createReview(Mockito.eq(bookingId), Mockito.eq(clientId),
                        Mockito.eq(rating), Mockito.eq(comment)))
                .thenReturn(review);

        String payload = "{"
                + "\"bookingId\":" + bookingId + ","
                + "\"clientId\":" + clientId + ","
                + "\"rating\":" + rating + ","
                + "\"comment\":\"" + comment + "\""
                + "}";

        mockMvc.perform(post("/api/reviews")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());
    }

    // Prueba para envío de reseña con rating inválido (fuera del rango 1-5)
    @Test
    @WithMockUser(roles = "CLIENT")
    public void testCreateReviewInvalidRating() throws Exception {
        Long bookingId = 101L;
        Long clientId = 1L;
        int rating = 6; // Valor inválido: debe estar entre 1 y 5
        String comment = "Excelente servicio";

        String payload = "{"
                + "\"bookingId\":" + bookingId + ","
                + "\"clientId\":" + clientId + ","
                + "\"rating\":" + rating + ","
                + "\"comment\":\"" + comment + "\""
                + "}";

        // Se espera una respuesta 400 Bad Request por la validación del rating
        mockMvc.perform(post("/api/reviews")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }
}
