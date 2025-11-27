package org.example.reservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.reservation.dto.ParkingSpotDTO;
import org.example.reservation.dto.ReservationDetailsDTO;
import org.example.reservation.dto.VehicleDTO;
import org.example.reservation.entity.Reservation;
import org.example.reservation.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
@ActiveProfiles("test")
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockBean
    private ReservationService reservationService;

    private Reservation reservation1;
    private Reservation reservation2;
    private ReservationDetailsDTO reservationDetails;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        LocalDateTime now = LocalDateTime.now();
        reservation1 = new Reservation(1L, 1L, 1L, now, now.plusHours(8), "ACTIVE");
        reservation2 = new Reservation(2L, 2L, 2L, now, now.plusHours(4), "COMPLETED");

        ParkingSpotDTO parkingSpot = new ParkingSpotDTO(1L, "A-101", 1, "RESERVED");
        VehicleDTO vehicle = new VehicleDTO(1L, "А123БВ", "Toyota Camry", "Black", 1L);
        reservationDetails = new ReservationDetailsDTO(reservation1, parkingSpot, vehicle);
    }

    @Test
    @WithMockUser
    void getAllReservations_ShouldReturnListOfReservations() throws Exception {
        List<Reservation> reservations = Arrays.asList(reservation1, reservation2);
        when(reservationService.getAllReservations()).thenReturn(reservations);

        mockMvc.perform(get("/reservations"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)));

        verify(reservationService, times(1)).getAllReservations();
    }

    @Test
    @WithMockUser
    void getReservationById_WhenExists_ShouldReturnReservation() throws Exception {
        when(reservationService.getReservationById(1L)).thenReturn(Optional.of(reservation1));

        mockMvc.perform(get("/reservations/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(reservationService, times(1)).getReservationById(1L);
    }

    @Test
    @WithMockUser
    void getReservationById_WhenNotExists_ShouldReturn404() throws Exception {
        when(reservationService.getReservationById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/reservations/999"))
                .andExpect(status().isNotFound());

        verify(reservationService, times(1)).getReservationById(999L);
    }

    @Test
    @WithMockUser
    void getReservationDetails_ShouldReturnFullDetails() throws Exception {
        when(reservationService.getReservationDetails(1L)).thenReturn(reservationDetails);

        mockMvc.perform(get("/reservations/1/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservation.id").value(1))
                .andExpect(jsonPath("$.parkingSpot.number").value("A-101"))
                .andExpect(jsonPath("$.vehicle.plateNumber").value("А123БВ"));

        verify(reservationService, times(1)).getReservationDetails(1L);
    }

    @Test
    @WithMockUser
    void getReservationsByParkingSpot_ShouldReturnFilteredReservations() throws Exception {
        List<Reservation> reservations = Arrays.asList(reservation1);
        when(reservationService.getReservationsByParkingSpot(1L)).thenReturn(reservations);

        mockMvc.perform(get("/reservations/parking-spot/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(reservationService, times(1)).getReservationsByParkingSpot(1L);
    }

    @Test
    @WithMockUser
    void getReservationsByVehicle_ShouldReturnFilteredReservations() throws Exception {
        List<Reservation> reservations = Arrays.asList(reservation1);
        when(reservationService.getReservationsByVehicle(1L)).thenReturn(reservations);

        mockMvc.perform(get("/reservations/vehicle/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(reservationService, times(1)).getReservationsByVehicle(1L);
    }

    @Test
    @WithMockUser
    void getReservationsByStatus_ShouldReturnFilteredReservations() throws Exception {
        List<Reservation> activeReservations = Arrays.asList(reservation1);
        when(reservationService.getReservationsByStatus("ACTIVE")).thenReturn(activeReservations);

        mockMvc.perform(get("/reservations/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(reservationService, times(1)).getReservationsByStatus("ACTIVE");
    }

    @Test
    @WithMockUser
    void createReservation_ShouldReturnCreatedReservation() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        Reservation newReservation = new Reservation(null, 1L, 1L, now, now.plusHours(8), "ACTIVE");
        Reservation savedReservation = new Reservation(3L, 1L, 1L, now, now.plusHours(8), "ACTIVE");
        
        when(reservationService.createReservation(any(Reservation.class))).thenReturn(savedReservation);

        mockMvc.perform(post("/reservations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newReservation)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));

        verify(reservationService, times(1)).createReservation(any(Reservation.class));
    }

    @Test
    @WithMockUser
    void updateReservation_ShouldReturnUpdatedReservation() throws Exception {
        when(reservationService.updateReservation(eq(1L), any(Reservation.class))).thenReturn(reservation1);

        mockMvc.perform(put("/reservations/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservation1)))
                .andExpect(status().isOk());

        verify(reservationService, times(1)).updateReservation(eq(1L), any(Reservation.class));
    }

    @Test
    @WithMockUser
    void completeReservation_ShouldReturnCompletedReservation() throws Exception {
        Reservation completed = new Reservation(1L, 1L, 1L, reservation1.getStartTime(), reservation1.getEndTime(), "COMPLETED");
        when(reservationService.completeReservation(1L)).thenReturn(completed);

        mockMvc.perform(patch("/reservations/1/complete")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(reservationService, times(1)).completeReservation(1L);
    }

    @Test
    @WithMockUser
    void cancelReservation_ShouldReturnCancelledReservation() throws Exception {
        Reservation cancelled = new Reservation(1L, 1L, 1L, reservation1.getStartTime(), reservation1.getEndTime(), "CANCELLED");
        when(reservationService.cancelReservation(1L)).thenReturn(cancelled);

        mockMvc.perform(patch("/reservations/1/cancel")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(reservationService, times(1)).cancelReservation(1L);
    }

    @Test
    @WithMockUser
    void deleteReservation_ShouldReturnNoContent() throws Exception {
        doNothing().when(reservationService).deleteReservation(1L);

        mockMvc.perform(delete("/reservations/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(reservationService, times(1)).deleteReservation(1L);
    }
}

