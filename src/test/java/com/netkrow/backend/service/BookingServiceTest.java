package com.netkrow.backend.service;

import static org.junit.jupiter.api.Assertions.*;

import com.netkrow.backend.model.Booking;
import com.netkrow.backend.model.User;
import com.netkrow.backend.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private BookingService bookingService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateBooking_Success() {
        // Datos de prueba
        Long clientId = 1L;
        Long specialistId = 2L;
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(2);

        // Creamos usuarios de prueba
        User client = new User();
        client.setId(clientId);
        User specialist = new User();
        specialist.setId(specialistId);

        // Configuramos el comportamiento de los mocks:
        // No hay reservas solapadas
        Mockito.when(bookingRepository.countOverlappingBookings(Mockito.eq(specialistId), Mockito.any(), Mockito.any()))
                .thenReturn(0L);
        Mockito.when(userService.findById(clientId))
                .thenReturn(Optional.of(client));
        Mockito.when(userService.findById(specialistId))
                .thenReturn(Optional.of(specialist));

        // Simulamos que al guardar la reserva, se asigna un ID
        Booking booking = new Booking();
        booking.setId(100L);
        Mockito.when(bookingRepository.save(Mockito.any(Booking.class))).thenReturn(booking);

        Booking result = bookingService.createBooking(clientId, specialistId, start, end);
        assertNotNull(result);
        assertEquals(100L, result.getId());
    }

    @Test
    public void testCreateBooking_Overlapping() {
        // Datos de prueba
        Long clientId = 1L;
        Long specialistId = 2L;
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(2);

        // Creamos usuarios de prueba
        User client = new User();
        client.setId(clientId);
        User specialist = new User();
        specialist.setId(specialistId);

        // Configuramos que ya exista una reserva solapada
        Mockito.when(bookingRepository.countOverlappingBookings(Mockito.eq(specialistId), Mockito.any(), Mockito.any()))
                .thenReturn(1L);
        Mockito.when(userService.findById(clientId))
                .thenReturn(Optional.of(client));
        Mockito.when(userService.findById(specialistId))
                .thenReturn(Optional.of(specialist));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingService.createBooking(clientId, specialistId, start, end);
        });
        assertEquals("El especialista ya tiene una reserva en ese intervalo.", exception.getMessage());
    }
}
