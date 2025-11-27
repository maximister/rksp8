package org.example.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.reservation.entity.Reservation;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDetailsDTO {
    private Reservation reservation;
    private ParkingSpotDTO parkingSpot;
    private VehicleDTO vehicle;
}




