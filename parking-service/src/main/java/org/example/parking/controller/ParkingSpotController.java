package org.example.parking.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.example.parking.entity.ParkingSpot;
import org.example.parking.service.ParkingSpotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/spots")
@RequiredArgsConstructor
public class ParkingSpotController {
    private final ParkingSpotService parkingSpotService;

    @GetMapping
    public List<ParkingSpot> getAllSpots() {
        return parkingSpotService.getAllSpots();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParkingSpot> getSpotById(@PathVariable Long id) {
        return parkingSpotService.getSpotById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public List<ParkingSpot> getSpotsByStatus(@PathVariable String status) {
        return parkingSpotService.getSpotsByStatus(status);
    }

    @GetMapping("/floor/{floor}")
    public List<ParkingSpot> getSpotsByFloor(@PathVariable Integer floor) {
        return parkingSpotService.getSpotsByFloor(floor);
    }

    @PostMapping
    public ParkingSpot createSpot(@RequestBody ParkingSpot spot) {
        return parkingSpotService.createSpot(spot);
    }

    @PutMapping("/{id}")
    public ParkingSpot updateSpot(@PathVariable Long id, @RequestBody ParkingSpot spot) {
        return parkingSpotService.updateSpot(id, spot);
    }

    @PatchMapping("/{id}/status")
    public ParkingSpot updateStatus(@PathVariable Long id, @RequestParam String status) {
        return parkingSpotService.updateStatus(id, status);
    }

    @PutMapping("/{id}/status")
    public ParkingSpot updateStatusPut(@PathVariable Long id, @RequestParam String status) {
        return parkingSpotService.updateStatus(id, status);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpot(@PathVariable Long id) {
        parkingSpotService.deleteSpot(id);
        return ResponseEntity.noContent().build();
    }
}




