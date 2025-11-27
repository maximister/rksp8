package org.example.reservation.client;

import org.example.reservation.dto.ParkingSpotDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "parking-service")
public interface ParkingServiceClient {
    
    @GetMapping("/spots/{id}")
    ParkingSpotDTO getParkingSpot(@PathVariable Long id);
    
    @PatchMapping("/spots/{id}/status")
    ParkingSpotDTO updateParkingSpotStatus(@PathVariable Long id, @RequestParam String status);
}




