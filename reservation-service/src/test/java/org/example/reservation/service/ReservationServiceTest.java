package org.example.reservation.service;

import org.example.reservation.client.ParkingServiceClient;
import org.example.reservation.client.VehicleServiceClient;
import org.example.reservation.dto.ParkingSpotDTO;
import org.example.reservation.dto.ReservationDetailsDTO;
import org.example.reservation.dto.VehicleDTO;
import org.example.reservation.entity.Reservation;
import org.example.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ParkingServiceClient parkingServiceClient;

    @Mock
    private VehicleServiceClient vehicleServiceClient;

    @InjectMocks
    private ReservationService reservationService;

    private Reservation reservation1;
    private Reservation reservation2;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        reservation1 = new Reservation(1L, 1L, 1L, now, now.plusHours(8), "ACTIVE");
        reservation2 = new Reservation(2L, 2L, 2L, now, now.plusHours(4), "COMPLETED");
    }

    @Test
    void getAllReservations_ShouldReturnAllReservations() {
        // Given
        List<Reservation> reservations = Arrays.asList(reservation1, reservation2);
        when(reservationRepository.findAll()).thenReturn(reservations);

        // When
        List<Reservation> result = reservationService.getAllReservations();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(reservation1, reservation2);
        verify(reservationRepository, times(1)).findAll();
    }

    @Test
    void getReservationById_WhenExists_ShouldReturnReservation() {
        // Given
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation1));

        // When
        Optional<Reservation> result = reservationService.getReservationById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo("ACTIVE");
        verify(reservationRepository, times(1)).findById(1L);
    }

    @Test
    void getReservationDetails_ShouldFetchDataFromMultipleServices() {
        // Given
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation1));
        
        ParkingSpotDTO parkingSpot = new ParkingSpotDTO(1L, "A-101", 1, "RESERVED");
        VehicleDTO vehicle = new VehicleDTO(1L, "А123БВ", "Toyota Camry", "Black", 1L);
        
        when(parkingServiceClient.getParkingSpot(1L)).thenReturn(parkingSpot);
        when(vehicleServiceClient.getVehicle(1L)).thenReturn(vehicle);

        // When
        ReservationDetailsDTO result = reservationService.getReservationDetails(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getReservation().getId()).isEqualTo(1L);
        assertThat(result.getParkingSpot().getNumber()).isEqualTo("A-101");
        assertThat(result.getVehicle().getPlateNumber()).isEqualTo("А123БВ");
        
        verify(reservationRepository, times(1)).findById(1L);
        verify(parkingServiceClient, times(1)).getParkingSpot(1L);
        verify(vehicleServiceClient, times(1)).getVehicle(1L);
    }

    @Test
    void getReservationsByParkingSpot_ShouldReturnFilteredReservations() {
        // Given
        List<Reservation> reservations = Arrays.asList(reservation1);
        when(reservationRepository.findByParkingSpotId(1L)).thenReturn(reservations);

        // When
        List<Reservation> result = reservationService.getReservationsByParkingSpot(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getParkingSpotId()).isEqualTo(1L);
        verify(reservationRepository, times(1)).findByParkingSpotId(1L);
    }

    @Test
    void getReservationsByVehicle_ShouldReturnFilteredReservations() {
        // Given
        List<Reservation> reservations = Arrays.asList(reservation1);
        when(reservationRepository.findByVehicleId(1L)).thenReturn(reservations);

        // When
        List<Reservation> result = reservationService.getReservationsByVehicle(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getVehicleId()).isEqualTo(1L);
        verify(reservationRepository, times(1)).findByVehicleId(1L);
    }

    @Test
    void getReservationsByStatus_ShouldReturnFilteredReservations() {
        // Given
        List<Reservation> activeReservations = Arrays.asList(reservation1);
        when(reservationRepository.findByStatus("ACTIVE")).thenReturn(activeReservations);

        // When
        List<Reservation> result = reservationService.getReservationsByStatus("ACTIVE");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("ACTIVE");
        verify(reservationRepository, times(1)).findByStatus("ACTIVE");
    }

    @Test
    void createReservation_ShouldSaveAndUpdateParkingSpot() {
        // Given
        Reservation newReservation = new Reservation(null, 1L, 1L, now, now.plusHours(8), "ACTIVE");
        Reservation savedReservation = new Reservation(3L, 1L, 1L, now, now.plusHours(8), "ACTIVE");
        ParkingSpotDTO parkingSpot = new ParkingSpotDTO(1L, "A-101", 1, "RESERVED");
        
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);
        when(parkingServiceClient.updateParkingSpotStatus(1L, "RESERVED")).thenReturn(parkingSpot);

        // When
        Reservation result = reservationService.createReservation(newReservation);

        // Then
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(parkingServiceClient, times(1)).updateParkingSpotStatus(1L, "RESERVED");
    }

    @Test
    void updateReservation_WhenExists_ShouldUpdateAndReturn() {
        // Given
        Reservation updatedDetails = new Reservation(null, 1L, 1L, now, now.plusHours(10), "ACTIVE");
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation1));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation1);

        // When
        Reservation result = reservationService.updateReservation(1L, updatedDetails);

        // Then
        assertThat(result.getEndTime()).isEqualTo(now.plusHours(10));
        verify(reservationRepository, times(1)).findById(1L);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    void completeReservation_ShouldUpdateStatusAndFreeParkingSpot() {
        // Given
        ParkingSpotDTO parkingSpot = new ParkingSpotDTO(1L, "A-101", 1, "FREE");
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation1));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation1);
        when(parkingServiceClient.updateParkingSpotStatus(1L, "FREE")).thenReturn(parkingSpot);

        // When
        Reservation result = reservationService.completeReservation(1L);

        // Then
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        verify(reservationRepository, times(1)).findById(1L);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(parkingServiceClient, times(1)).updateParkingSpotStatus(1L, "FREE");
    }

    @Test
    void cancelReservation_ShouldUpdateStatusAndFreeParkingSpot() {
        // Given
        ParkingSpotDTO parkingSpot = new ParkingSpotDTO(1L, "A-101", 1, "FREE");
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation1));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation1);
        when(parkingServiceClient.updateParkingSpotStatus(1L, "FREE")).thenReturn(parkingSpot);

        // When
        Reservation result = reservationService.cancelReservation(1L);

        // Then
        assertThat(result.getStatus()).isEqualTo("CANCELLED");
        verify(reservationRepository, times(1)).findById(1L);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(parkingServiceClient, times(1)).updateParkingSpotStatus(1L, "FREE");
    }

    @Test
    void deleteReservation_ShouldCallRepository() {
        // Given
        doNothing().when(reservationRepository).deleteById(1L);

        // When
        reservationService.deleteReservation(1L);

        // Then
        verify(reservationRepository, times(1)).deleteById(1L);
    }

    @Test
    void updateReservation_WhenNotExists_ShouldThrowException() {
        // Given
        Reservation updatedDetails = new Reservation(null, 1L, 1L, now, now.plusHours(10), "ACTIVE");
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reservationService.updateReservation(999L, updatedDetails))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Reservation not found");
        
        verify(reservationRepository, times(1)).findById(999L);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }
}

