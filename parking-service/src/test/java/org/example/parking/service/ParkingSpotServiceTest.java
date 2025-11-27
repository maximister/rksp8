package org.example.parking.service;

import org.example.parking.entity.ParkingSpot;
import org.example.parking.repository.ParkingSpotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingSpotServiceTest {

    @Mock
    private ParkingSpotRepository parkingSpotRepository;

    @InjectMocks
    private ParkingSpotService parkingSpotService;

    private ParkingSpot parkingSpot1;
    private ParkingSpot parkingSpot2;

    @BeforeEach
    void setUp() {
        parkingSpot1 = new ParkingSpot(1L, "A-101", 1, "FREE");
        parkingSpot2 = new ParkingSpot(2L, "A-102", 1, "OCCUPIED");
    }

    @Test
    void getAllSpots_ShouldReturnAllSpots() {
        // Given
        List<ParkingSpot> spots = Arrays.asList(parkingSpot1, parkingSpot2);
        when(parkingSpotRepository.findAll()).thenReturn(spots);

        // When
        List<ParkingSpot> result = parkingSpotService.getAllSpots();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(parkingSpot1, parkingSpot2);
        verify(parkingSpotRepository, times(1)).findAll();
    }

    @Test
    void getSpotById_WhenExists_ShouldReturnSpot() {
        // Given
        when(parkingSpotRepository.findById(1L)).thenReturn(Optional.of(parkingSpot1));

        // When
        Optional<ParkingSpot> result = parkingSpotService.getSpotById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getNumber()).isEqualTo("A-101");
        verify(parkingSpotRepository, times(1)).findById(1L);
    }

    @Test
    void getSpotById_WhenNotExists_ShouldReturnEmpty() {
        // Given
        when(parkingSpotRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<ParkingSpot> result = parkingSpotService.getSpotById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(parkingSpotRepository, times(1)).findById(999L);
    }

    @Test
    void getSpotsByStatus_ShouldReturnFilteredSpots() {
        // Given
        List<ParkingSpot> freeSpots = Arrays.asList(parkingSpot1);
        when(parkingSpotRepository.findByStatus("FREE")).thenReturn(freeSpots);

        // When
        List<ParkingSpot> result = parkingSpotService.getSpotsByStatus("FREE");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("FREE");
        verify(parkingSpotRepository, times(1)).findByStatus("FREE");
    }

    @Test
    void getSpotsByFloor_ShouldReturnFilteredSpots() {
        // Given
        List<ParkingSpot> floor1Spots = Arrays.asList(parkingSpot1, parkingSpot2);
        when(parkingSpotRepository.findByFloor(1)).thenReturn(floor1Spots);

        // When
        List<ParkingSpot> result = parkingSpotService.getSpotsByFloor(1);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(spot -> spot.getFloor() == 1);
        verify(parkingSpotRepository, times(1)).findByFloor(1);
    }

    @Test
    void createSpot_ShouldSaveAndReturnSpot() {
        // Given
        ParkingSpot newSpot = new ParkingSpot(null, "B-201", 2, "FREE");
        ParkingSpot savedSpot = new ParkingSpot(3L, "B-201", 2, "FREE");
        when(parkingSpotRepository.save(any(ParkingSpot.class))).thenReturn(savedSpot);

        // When
        ParkingSpot result = parkingSpotService.createSpot(newSpot);

        // Then
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getNumber()).isEqualTo("B-201");
        verify(parkingSpotRepository, times(1)).save(any(ParkingSpot.class));
    }

    @Test
    void updateSpot_WhenExists_ShouldUpdateAndReturn() {
        // Given
        ParkingSpot updatedDetails = new ParkingSpot(null, "A-101-VIP", 1, "RESERVED");
        when(parkingSpotRepository.findById(1L)).thenReturn(Optional.of(parkingSpot1));
        when(parkingSpotRepository.save(any(ParkingSpot.class))).thenReturn(parkingSpot1);

        // When
        ParkingSpot result = parkingSpotService.updateSpot(1L, updatedDetails);

        // Then
        assertThat(result.getNumber()).isEqualTo("A-101-VIP");
        assertThat(result.getStatus()).isEqualTo("RESERVED");
        verify(parkingSpotRepository, times(1)).findById(1L);
        verify(parkingSpotRepository, times(1)).save(any(ParkingSpot.class));
    }

    @Test
    void updateSpot_WhenNotExists_ShouldThrowException() {
        // Given
        ParkingSpot updatedDetails = new ParkingSpot(null, "A-101-VIP", 1, "RESERVED");
        when(parkingSpotRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> parkingSpotService.updateSpot(999L, updatedDetails))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Parking spot not found");
        
        verify(parkingSpotRepository, times(1)).findById(999L);
        verify(parkingSpotRepository, never()).save(any(ParkingSpot.class));
    }

    @Test
    void updateStatus_WhenExists_ShouldUpdateStatus() {
        // Given
        when(parkingSpotRepository.findById(1L)).thenReturn(Optional.of(parkingSpot1));
        when(parkingSpotRepository.save(any(ParkingSpot.class))).thenReturn(parkingSpot1);

        // When
        ParkingSpot result = parkingSpotService.updateStatus(1L, "OCCUPIED");

        // Then
        assertThat(result.getStatus()).isEqualTo("OCCUPIED");
        verify(parkingSpotRepository, times(1)).findById(1L);
        verify(parkingSpotRepository, times(1)).save(any(ParkingSpot.class));
    }

    @Test
    void updateStatus_WhenNotExists_ShouldThrowException() {
        // Given
        when(parkingSpotRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> parkingSpotService.updateStatus(999L, "OCCUPIED"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Parking spot not found");
        
        verify(parkingSpotRepository, times(1)).findById(999L);
        verify(parkingSpotRepository, never()).save(any(ParkingSpot.class));
    }

    @Test
    void deleteSpot_ShouldCallRepository() {
        // Given
        doNothing().when(parkingSpotRepository).deleteById(1L);

        // When
        parkingSpotService.deleteSpot(1L);

        // Then
        verify(parkingSpotRepository, times(1)).deleteById(1L);
    }
}

