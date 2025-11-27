package org.example.reservation.repository;

import org.example.reservation.entity.Reservation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    private Reservation reservation1;
    private Reservation reservation2;
    private Reservation reservation3;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        
        now = LocalDateTime.now();
        reservation1 = new Reservation(null, 1L, 1L, now, now.plusHours(8), "ACTIVE");
        reservation2 = new Reservation(null, 2L, 2L, now, now.plusHours(4), "COMPLETED");
        reservation3 = new Reservation(null, 1L, 3L, now.plusDays(1), now.plusDays(1).plusHours(8), "ACTIVE");

        reservationRepository.save(reservation1);
        reservationRepository.save(reservation2);
        reservationRepository.save(reservation3);
    }

    @Test
    void findAll_ShouldReturnAllReservations() {
        // When
        List<Reservation> reservations = reservationRepository.findAll();

        // Then
        assertThat(reservations).hasSize(3);
    }

    @Test
    void findById_WhenExists_ShouldReturnReservation() {
        // Given
        Long savedId = reservation1.getId();

        // When
        Optional<Reservation> found = reservationRepository.findById(savedId);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getParkingSpotId()).isEqualTo(1L);
        assertThat(found.get().getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void findById_WhenNotExists_ShouldReturnEmpty() {
        // When
        Optional<Reservation> found = reservationRepository.findById(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByParkingSpotId_ShouldReturnReservationsForSpot() {
        // When
        List<Reservation> reservations = reservationRepository.findByParkingSpotId(1L);

        // Then
        assertThat(reservations).hasSize(2);
        assertThat(reservations).allMatch(r -> r.getParkingSpotId().equals(1L));
    }

    @Test
    void findByParkingSpotId_WhenNoReservations_ShouldReturnEmpty() {
        // When
        List<Reservation> reservations = reservationRepository.findByParkingSpotId(999L);

        // Then
        assertThat(reservations).isEmpty();
    }

    @Test
    void findByVehicleId_ShouldReturnReservationsForVehicle() {
        // When
        List<Reservation> reservations = reservationRepository.findByVehicleId(1L);

        // Then
        assertThat(reservations).hasSize(1);
        assertThat(reservations.get(0).getVehicleId()).isEqualTo(1L);
    }

    @Test
    void findByVehicleId_WhenNoReservations_ShouldReturnEmpty() {
        // When
        List<Reservation> reservations = reservationRepository.findByVehicleId(999L);

        // Then
        assertThat(reservations).isEmpty();
    }

    @Test
    void findByStatus_ShouldReturnFilteredReservations() {
        // When
        List<Reservation> activeReservations = reservationRepository.findByStatus("ACTIVE");

        // Then
        assertThat(activeReservations).hasSize(2);
        assertThat(activeReservations).allMatch(r -> r.getStatus().equals("ACTIVE"));
    }

    @Test
    void findByStatus_WhenNoMatches_ShouldReturnEmpty() {
        // When
        List<Reservation> cancelledReservations = reservationRepository.findByStatus("CANCELLED");

        // Then
        assertThat(cancelledReservations).isEmpty();
    }

    @Test
    void save_ShouldPersistNewReservation() {
        // Given
        Reservation newReservation = new Reservation(null, 3L, 4L, now, now.plusHours(6), "ACTIVE");

        // When
        Reservation saved = reservationRepository.save(newReservation);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getParkingSpotId()).isEqualTo(3L);
        assertThat(reservationRepository.findAll()).hasSize(4);
    }

    @Test
    void save_ShouldUpdateExistingReservation() {
        // Given
        Long reservationId = reservation1.getId();
        reservation1.setStatus("COMPLETED");

        // When
        Reservation updated = reservationRepository.save(reservation1);

        // Then
        assertThat(updated.getId()).isEqualTo(reservationId);
        assertThat(updated.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void deleteById_ShouldRemoveReservation() {
        // Given
        Long reservationId = reservation1.getId();
        assertThat(reservationRepository.findById(reservationId)).isPresent();

        // When
        reservationRepository.deleteById(reservationId);

        // Then
        assertThat(reservationRepository.findById(reservationId)).isEmpty();
        assertThat(reservationRepository.findAll()).hasSize(2);
    }
}

