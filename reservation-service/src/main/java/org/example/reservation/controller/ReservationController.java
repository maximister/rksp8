package org.example.reservation.controller;

import lombok.RequiredArgsConstructor;
import org.example.reservation.dto.ReservationDetailsDTO;
import org.example.reservation.entity.Reservation;
import org.example.reservation.service.ReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;

    @GetMapping
    public List<Reservation> getAllReservations() {
        return reservationService.getAllReservations();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(@PathVariable Long id) {
        return reservationService.getReservationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/details")
    public ReservationDetailsDTO getReservationDetails(@PathVariable Long id) {
        return reservationService.getReservationDetails(id);
    }

    @GetMapping("/parking-spot/{parkingSpotId}")
    public List<Reservation> getReservationsByParkingSpot(@PathVariable Long parkingSpotId) {
        return reservationService.getReservationsByParkingSpot(parkingSpotId);
    }

    @GetMapping("/vehicle/{vehicleId}")
    public List<Reservation> getReservationsByVehicle(@PathVariable Long vehicleId) {
        return reservationService.getReservationsByVehicle(vehicleId);
    }

    @GetMapping("/status/{status}")
    public List<Reservation> getReservationsByStatus(@PathVariable String status) {
        return reservationService.getReservationsByStatus(status);
    }

    @PostMapping
    public Reservation createReservation(@RequestBody Reservation reservation) {
        return reservationService.createReservation(reservation);
    }

    @PutMapping("/{id}")
    public Reservation updateReservation(@PathVariable Long id, @RequestBody Reservation reservation) {
        return reservationService.updateReservation(id, reservation);
    }

    @PatchMapping("/{id}/complete")
    public Reservation completeReservation(@PathVariable Long id) {
        return reservationService.completeReservation(id);
    }

    @PatchMapping("/{id}/cancel")
    public Reservation cancelReservation(@PathVariable Long id) {
        return reservationService.cancelReservation(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}




