package org.example.reservation.repository;

import org.example.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByParkingSpotId(Long parkingSpotId);
    List<Reservation> findByVehicleId(Long vehicleId);
    List<Reservation> findByStatus(String status);
}




