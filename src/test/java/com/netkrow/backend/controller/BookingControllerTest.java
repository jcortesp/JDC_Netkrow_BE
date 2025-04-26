package com.netkrow.backend.controller;

import com.netkrow.backend.model.Booking;
import com.netkrow.backend.security.JwtUtils;
import com.netkrow.backend.service.BookingService;
import com.netkrow.backend.service.UserService;
import com.netkrow.backend.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@Import(SecurityConfig.class) // Importamos la configuración de seguridad
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "CLIENT")
    public void testCreateBooking() throws Exception {
        Long clientId = 1L;
        Long specialistId = 2L;
        String startDateTime = LocalDateTime.now().plusDays(1)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String endDateTime = LocalDateTime.now().plusDays(1).plusHours(2)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        Booking booking = new Booking();
        booking.setId(100L);
        booking.setClientId(clientId);
        booking.setSpecialistId(specialistId);
        booking.setStartTime(LocalDateTime.parse(startDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        booking.setEndTime(LocalDateTime.parse(endDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        booking.setStatus("PENDING");

        Mockito.when(bookingService.createBooking(Mockito.eq(clientId), Mockito.eq(specialistId),
                Mockito.any(), Mockito.any())).thenReturn(booking);

        String requestPayload = "{"
                + "\"clientId\":" + clientId + ","
                + "\"specialistId\":" + specialistId + ","
                + "\"startDateTime\":\"" + startDateTime + "\","
                + "\"endDateTime\":\"" + endDateTime + "\""
                + "}";

        mockMvc.perform(post("/api/bookings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestPayload))
                .andExpect(status().isOk());
    }
}
