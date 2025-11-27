package org.example.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParkingSpotDTO {
    private Long id;
    private String number;
    private Integer floor;
    private String status;
}




