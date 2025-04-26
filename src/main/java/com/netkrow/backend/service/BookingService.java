// BookingService.java
package com.netkrow.backend.service;

import com.netkrow.backend.model.Booking;
import com.netkrow.backend.model.User;
import com.netkrow.backend.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserService userService;

    public Booking createBooking(Long clientId, Long specialistId, LocalDateTime start, LocalDateTime end) {
        User client = userService.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        User specialist = userService.findById(specialistId)
                .orElseThrow(() -> new RuntimeException("Especialista no encontrado"));

        if (!isSpecialistAvailable(specialistId, start, end)) {
            throw new RuntimeException("El especialista ya tiene una reserva en ese intervalo.");
        }

        Booking booking = new Booking();
        booking.setClientId(client.getId());
        booking.setSpecialistId(specialist.getId());
        booking.setStartTime(start);
        booking.setEndTime(end);
        booking.setStatus("PENDING");

        return bookingRepository.save(booking);
    }

    public Optional<Booking> getBooking(Long bookingId) {
        return bookingRepository.findById(bookingId);
    }

    public Booking updateBookingStatus(Long bookingId, String newStatus) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking no encontrado"));
        booking.setStatus(newStatus);
        return bookingRepository.save(booking);
    }

    public Iterable<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public boolean isSpecialistAvailable(Long specialistId, LocalDateTime newStart, LocalDateTime newEnd) {
        long overlapping = bookingRepository.countOverlappingBookings(specialistId, newStart, newEnd);
        return overlapping == 0;
    }

    public Booking confirmReservation(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking no encontrado"));
        if (!"PENDING".equalsIgnoreCase(booking.getStatus())) {
            throw new RuntimeException("Solo se pueden confirmar reservas en estado PENDING.");
        }
        booking.setStatus("CONFIRMED");
        return bookingRepository.save(booking);
    }

    // Método para obtener reservas de un mes específico
    public List<Booking> getBookingsByMonth(int year, int month) {
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1).minusSeconds(1);
        return bookingRepository.findAllByStartTimeBetween(start, end);
    }

    // NUEVO: Método para modificar una reserva (solo en estado PENDING)
    public Booking modifyReservation(Long bookingId, LocalDateTime newStart, LocalDateTime newEnd) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));
        if (!"PENDING".equalsIgnoreCase(booking.getStatus())) {
            throw new RuntimeException("Solo se pueden modificar reservas en estado PENDING");
        }
        if (!isSpecialistAvailable(booking.getSpecialistId(), newStart, newEnd)) {
            throw new RuntimeException("El especialista no está disponible en el nuevo horario");
        }
        booking.setStartTime(newStart);
        booking.setEndTime(newEnd);
        booking.setStatus("MODIFIED");
        return bookingRepository.save(booking);
    }

    // NUEVO: Método para cancelar una reserva
    public Booking cancelReservation(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));
        if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) {
            throw new RuntimeException("La reserva ya está cancelada");
        }
        booking.setStatus("CANCELLED");
        return bookingRepository.save(booking);
    }
}
