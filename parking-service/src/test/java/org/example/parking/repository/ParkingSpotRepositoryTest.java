package org.example.parking.repository;

import org.example.parking.entity.ParkingSpot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ParkingSpotRepositoryTest {

    @Autowired
    private ParkingSpotRepository parkingSpotRepository;

    private ParkingSpot spot1;
    private ParkingSpot spot2;
    private ParkingSpot spot3;

    @BeforeEach
    void setUp() {
        parkingSpotRepository.deleteAll();
        
        spot1 = new ParkingSpot(null, "A-101", 1, "FREE");
        spot2 = new ParkingSpot(null, "A-102", 1, "OCCUPIED");
        spot3 = new ParkingSpot(null, "B-201", 2, "FREE");

        parkingSpotRepository.save(spot1);
        parkingSpotRepository.save(spot2);
        parkingSpotRepository.save(spot3);
    }

    @Test
    void findAll_ShouldReturnAllSpots() {
        List<ParkingSpot> spots = parkingSpotRepository.findAll();

        assertThat(spots).hasSize(3);
    }

    @Test
    void findById_WhenExists_ShouldReturnSpot() {
        Long savedId = spot1.getId();

        Optional<ParkingSpot> found = parkingSpotRepository.findById(savedId);

        assertThat(found).isPresent();
        assertThat(found.get().getNumber()).isEqualTo("A-101");
    }

    @Test
    void findById_WhenNotExists_ShouldReturnEmpty() {
        Optional<ParkingSpot> found = parkingSpotRepository.findById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void findByStatus_ShouldReturnFilteredSpots() {
        List<ParkingSpot> freeSpots = parkingSpotRepository.findByStatus("FREE");

        assertThat(freeSpots).hasSize(2);
        assertThat(freeSpots).allMatch(spot -> spot.getStatus().equals("FREE"));
    }

    @Test
    void findByStatus_WhenNoMatches_ShouldReturnEmpty() {
        List<ParkingSpot> reservedSpots = parkingSpotRepository.findByStatus("RESERVED");

        assertThat(reservedSpots).isEmpty();
    }

    @Test
    void findByFloor_ShouldReturnFilteredSpots() {
        List<ParkingSpot> floor1Spots = parkingSpotRepository.findByFloor(1);

        assertThat(floor1Spots).hasSize(2);
        assertThat(floor1Spots).allMatch(spot -> spot.getFloor() == 1);
    }

    @Test
    void findByFloor_WhenNoMatches_ShouldReturnEmpty() {
        List<ParkingSpot> floor3Spots = parkingSpotRepository.findByFloor(3);

        assertThat(floor3Spots).isEmpty();
    }

    @Test
    void save_ShouldPersistNewSpot() {
        ParkingSpot newSpot = new ParkingSpot(null, "C-301", 3, "FREE");

        ParkingSpot saved = parkingSpotRepository.save(newSpot);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getNumber()).isEqualTo("C-301");
        assertThat(parkingSpotRepository.findAll()).hasSize(4);
    }

    @Test
    void save_ShouldUpdateExistingSpot() {
        Long spotId = spot1.getId();
        spot1.setStatus("OCCUPIED");

        ParkingSpot updated = parkingSpotRepository.save(spot1);

        assertThat(updated.getId()).isEqualTo(spotId);
        assertThat(updated.getStatus()).isEqualTo("OCCUPIED");
    }

    @Test
    void deleteById_ShouldRemoveSpot() {
        Long spotId = spot1.getId();
        assertThat(parkingSpotRepository.findById(spotId)).isPresent();

        parkingSpotRepository.deleteById(spotId);

        assertThat(parkingSpotRepository.findById(spotId)).isEmpty();
        assertThat(parkingSpotRepository.findAll()).hasSize(2);
    }
}

