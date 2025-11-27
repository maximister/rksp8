package org.example.reservation.service;

import lombok.RequiredArgsConstructor;
import org.example.reservation.client.ParkingServiceClient;
import org.example.reservation.client.VehicleServiceClient;
import org.example.reservation.dto.ParkingSpotDTO;
import org.example.reservation.dto.ReservationDetailsDTO;
import org.example.reservation.dto.VehicleDTO;
import org.example.reservation.entity.Reservation;
import org.example.reservation.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ParkingServiceClient parkingServiceClient;
    private final VehicleServiceClient vehicleServiceClient;

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public Optional<Reservation> getReservationById(Long id) {
        return reservationRepository.findById(id);
    }

    public ReservationDetailsDTO getReservationDetails(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        
        ParkingSpotDTO parkingSpot = parkingServiceClient.getParkingSpot(reservation.getParkingSpotId());
        VehicleDTO vehicle = vehicleServiceClient.getVehicle(reservation.getVehicleId());
        
        return new ReservationDetailsDTO(reservation, parkingSpot, vehicle);
    }

    public List<Reservation> getReservationsByParkingSpot(Long parkingSpotId) {
        return reservationRepository.findByParkingSpotId(parkingSpotId);
    }

    public List<Reservation> getReservationsByVehicle(Long vehicleId) {
        return reservationRepository.findByVehicleId(vehicleId);
    }

    public List<Reservation> getReservationsByStatus(String status) {
        return reservationRepository.findByStatus(status);
    }

    public Reservation createReservation(Reservation reservation) {
        // Обновляем статус парковочного места на RESERVED
        parkingServiceClient.updateParkingSpotStatus(reservation.getParkingSpotId(), "RESERVED");
        reservation.setStatus("ACTIVE");
        return reservationRepository.save(reservation);
    }

    public Reservation updateReservation(Long id, Reservation reservationDetails) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        reservation.setParkingSpotId(reservationDetails.getParkingSpotId());
        reservation.setVehicleId(reservationDetails.getVehicleId());
        reservation.setStartTime(reservationDetails.getStartTime());
        reservation.setEndTime(reservationDetails.getEndTime());
        reservation.setStatus(reservationDetails.getStatus());
        return reservationRepository.save(reservation);
    }

    public Reservation completeReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        
        // Освобождаем парковочное место
        parkingServiceClient.updateParkingSpotStatus(reservation.getParkingSpotId(), "FREE");
        reservation.setStatus("COMPLETED");
        return reservationRepository.save(reservation);
    }

    public Reservation cancelReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        
        // Освобождаем парковочное место
        parkingServiceClient.updateParkingSpotStatus(reservation.getParkingSpotId(), "FREE");
        reservation.setStatus("CANCELLED");
        return reservationRepository.save(reservation);
    }

    public void deleteReservation(Long id) {
        reservationRepository.deleteById(id);
    }
}




