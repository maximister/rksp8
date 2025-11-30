package org.example.reservation.client;

import org.example.reservation.dto.ParkingSpotDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "parking-service")
public interface ParkingServiceClient {
    
    @GetMapping("/spots/{id}")
    ParkingSpotDTO getParkingSpot(@PathVariable Long id);
    
    @PutMapping("/spots/{id}/status")
    ParkingSpotDTO updateParkingSpotStatus(@PathVariable Long id, @RequestParam String status);
}




