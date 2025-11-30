package org.example.reservation.client;

import lombok.RequiredArgsConstructor;
import org.example.reservation.dto.ParkingSpotDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class ParkingServiceRestClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${services.parking-service.url:http://localhost:8081}")
    private String parkingServiceUrl;
    
    public ParkingSpotDTO getParkingSpot(Long id) {
        String url = parkingServiceUrl + "/spots/" + id;
        return restTemplate.getForObject(url, ParkingSpotDTO.class);
    }
    
    public ParkingSpotDTO updateParkingSpotStatus(Long id, String status) {
        String url = parkingServiceUrl + "/spots/" + id + "/status?status=" + status;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        return restTemplate.exchange(
            url,
            HttpMethod.PUT,
            entity,
            ParkingSpotDTO.class
        ).getBody();
    }
}

