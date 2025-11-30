package org.example.reservation.client;

import lombok.RequiredArgsConstructor;
import org.example.reservation.dto.VehicleDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class VehicleServiceRestClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${services.vehicle-service.url:http://localhost:8082}")
    private String vehicleServiceUrl;
    
    public VehicleDTO getVehicle(Long id) {
        String url = vehicleServiceUrl + "/vehicles/" + id;
        return restTemplate.getForObject(url, VehicleDTO.class);
    }
}

