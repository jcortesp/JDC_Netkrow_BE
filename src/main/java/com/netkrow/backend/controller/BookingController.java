// BookingController.java
package com.netkrow.backend.controller;

import com.netkrow.backend.model.Booking;
import com.netkrow.backend.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    public static class BookingRequest {
        private Long clientId;
        private Long specialistId;
        private String startDateTime;
        private String endDateTime;
        // Getters y setters...
        public Long getClientId() { return clientId; }
        public void setClientId(Long clientId) { this.clientId = clientId; }
        public Long getSpecialistId() { return specialistId; }
        public void setSpecialistId(Long specialistId) { this.specialistId = specialistId; }
        public String getStartDateTime() { return startDateTime; }
        public void setStartDateTime(String startDateTime) { this.startDateTime = startDateTime; }
        public String getEndDateTime() { return endDateTime; }
        public void setEndDateTime(String endDateTime) { this.endDateTime = endDateTime; }
    }

    // NUEVO: DTO para modificación de reserva
    public static class BookingModificationRequest {
        private String newStartDateTime;
        private String newEndDateTime;
        // Getters y setters...
        public String getNewStartDateTime() { return newStartDateTime; }
        public void setNewStartDateTime(String newStartDateTime) { this.newStartDateTime = newStartDateTime; }
        public String getNewEndDateTime() { return newEndDateTime; }
        public void setNewEndDateTime(String newEndDateTime) { this.newEndDateTime = newEndDateTime; }
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime start = LocalDateTime.parse(request.getStartDateTime(), formatter);
        LocalDateTime end = LocalDateTime.parse(request.getEndDateTime(), formatter);

        try {
            Booking booking = bookingService.createBooking(
                    request.getClientId(),
                    request.getSpecialistId(),
                    start,
                    end
            );
            return ResponseEntity.ok(booking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    // NUEVO: Endpoint para obtener reservas de un mes específico
    @GetMapping("/month")
    public ResponseEntity<?> getBookingsByMonth(@RequestParam int year, @RequestParam int month) {
        try {
            List<Booking> bookings = bookingService.getBookingsByMonth(year, month);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // NUEVO: Endpoint para modificar una reserva
    @PutMapping("/{bookingId}/modify")
    public ResponseEntity<?> modifyBooking(@PathVariable Long bookingId, @RequestBody BookingModificationRequest request) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime newStart = LocalDateTime.parse(request.getNewStartDateTime(), formatter);
        LocalDateTime newEnd = LocalDateTime.parse(request.getNewEndDateTime(), formatter);
        try {
            Booking updatedBooking = bookingService.modifyReservation(bookingId, newStart, newEnd);
            return ResponseEntity.ok(updatedBooking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // NUEVO: Endpoint para cancelar una reserva
    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Long bookingId) {
        try {
            Booking cancelledBooking = bookingService.cancelReservation(bookingId);
            return ResponseEntity.ok(cancelledBooking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
