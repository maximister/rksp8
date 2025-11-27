package org.example.parking.service;

import lombok.RequiredArgsConstructor;
import org.example.parking.entity.ParkingSpot;
import org.example.parking.repository.ParkingSpotRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ParkingSpotService {
    private final ParkingSpotRepository parkingSpotRepository;

    public List<ParkingSpot> getAllSpots() {
        return parkingSpotRepository.findAll();
    }

    public Optional<ParkingSpot> getSpotById(Long id) {
        return parkingSpotRepository.findById(id);
    }

    public List<ParkingSpot> getSpotsByStatus(String status) {
        return parkingSpotRepository.findByStatus(status);
    }

    public List<ParkingSpot> getSpotsByFloor(Integer floor) {
        return parkingSpotRepository.findByFloor(floor);
    }

    public ParkingSpot createSpot(ParkingSpot spot) {
        return parkingSpotRepository.save(spot);
    }

    public ParkingSpot updateSpot(Long id, ParkingSpot spotDetails) {
        ParkingSpot spot = parkingSpotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parking spot not found"));
        spot.setNumber(spotDetails.getNumber());
        spot.setFloor(spotDetails.getFloor());
        spot.setStatus(spotDetails.getStatus());
        return parkingSpotRepository.save(spot);
    }

    public void deleteSpot(Long id) {
        parkingSpotRepository.deleteById(id);
    }

    public ParkingSpot updateStatus(Long id, String status) {
        ParkingSpot spot = parkingSpotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parking spot not found"));
        spot.setStatus(status);
        return parkingSpotRepository.save(spot);
    }
}




