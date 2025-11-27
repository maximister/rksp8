package org.example.reservation.client;

import org.example.reservation.dto.VehicleDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "vehicle-service")
public interface VehicleServiceClient {
    
    @GetMapping("/vehicles/{id}")
    VehicleDTO getVehicle(@PathVariable Long id);
}




