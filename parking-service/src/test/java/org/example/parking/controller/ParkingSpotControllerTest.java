package org.example.parking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.parking.entity.ParkingSpot;
import org.example.parking.service.ParkingSpotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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

@WebMvcTest(ParkingSpotController.class)
@ActiveProfiles("test")
class ParkingSpotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ParkingSpotService parkingSpotService;

    private ParkingSpot parkingSpot1;
    private ParkingSpot parkingSpot2;

    @BeforeEach
    void setUp() {
        parkingSpot1 = new ParkingSpot(1L, "A-101", 1, "FREE");
        parkingSpot2 = new ParkingSpot(2L, "A-102", 1, "OCCUPIED");
    }

    @Test
    @WithMockUser
    void getAllSpots_ShouldReturnListOfSpots() throws Exception {
        List<ParkingSpot> spots = Arrays.asList(parkingSpot1, parkingSpot2);
        when(parkingSpotService.getAllSpots()).thenReturn(spots);

        mockMvc.perform(get("/spots"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].number").value("A-101"))
                .andExpect(jsonPath("$[1].number").value("A-102"));

        verify(parkingSpotService, times(1)).getAllSpots();
    }

    @Test
    @WithMockUser
    void getSpotById_WhenExists_ShouldReturnSpot() throws Exception {
        when(parkingSpotService.getSpotById(1L)).thenReturn(Optional.of(parkingSpot1));

        mockMvc.perform(get("/spots/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.number").value("A-101"))
                .andExpect(jsonPath("$.floor").value(1))
                .andExpect(jsonPath("$.status").value("FREE"));

        verify(parkingSpotService, times(1)).getSpotById(1L);
    }

    @Test
    @WithMockUser
    void getSpotById_WhenNotExists_ShouldReturn404() throws Exception {
        when(parkingSpotService.getSpotById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/spots/999"))
                .andExpect(status().isNotFound());

        verify(parkingSpotService, times(1)).getSpotById(999L);
    }

    @Test
    @WithMockUser
    void getSpotsByStatus_ShouldReturnFilteredSpots() throws Exception {
        List<ParkingSpot> freeSpots = Arrays.asList(parkingSpot1);
        when(parkingSpotService.getSpotsByStatus("FREE")).thenReturn(freeSpots);

        mockMvc.perform(get("/spots/status/FREE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("FREE"));

        verify(parkingSpotService, times(1)).getSpotsByStatus("FREE");
    }

    @Test
    @WithMockUser
    void getSpotsByFloor_ShouldReturnFilteredSpots() throws Exception {
        List<ParkingSpot> floor1Spots = Arrays.asList(parkingSpot1, parkingSpot2);
        when(parkingSpotService.getSpotsByFloor(1)).thenReturn(floor1Spots);

        mockMvc.perform(get("/spots/floor/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].floor").value(1));

        verify(parkingSpotService, times(1)).getSpotsByFloor(1);
    }

    @Test
    @WithMockUser
    void createSpot_ShouldReturnCreatedSpot() throws Exception {
        ParkingSpot newSpot = new ParkingSpot(null, "B-201", 2, "FREE");
        ParkingSpot savedSpot = new ParkingSpot(3L, "B-201", 2, "FREE");
        
        when(parkingSpotService.createSpot(any(ParkingSpot.class))).thenReturn(savedSpot);

        mockMvc.perform(post("/spots")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newSpot)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.number").value("B-201"))
                .andExpect(jsonPath("$.floor").value(2))
                .andExpect(jsonPath("$.status").value("FREE"));

        verify(parkingSpotService, times(1)).createSpot(any(ParkingSpot.class));
    }

    @Test
    @WithMockUser
    void updateSpot_ShouldReturnUpdatedSpot() throws Exception {
        ParkingSpot updatedSpot = new ParkingSpot(1L, "A-101-VIP", 1, "RESERVED");
        
        when(parkingSpotService.updateSpot(eq(1L), any(ParkingSpot.class))).thenReturn(updatedSpot);

        mockMvc.perform(put("/spots/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedSpot)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("A-101-VIP"))
                .andExpect(jsonPath("$.status").value("RESERVED"));

        verify(parkingSpotService, times(1)).updateSpot(eq(1L), any(ParkingSpot.class));
    }

    @Test
    @WithMockUser
    void updateStatus_ShouldReturnSpotWithNewStatus() throws Exception {
        ParkingSpot updatedSpot = new ParkingSpot(1L, "A-101", 1, "OCCUPIED");
        
        when(parkingSpotService.updateStatus(1L, "OCCUPIED")).thenReturn(updatedSpot);

        mockMvc.perform(patch("/spots/1/status")
                        .with(csrf())
                        .param("status", "OCCUPIED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OCCUPIED"));

        verify(parkingSpotService, times(1)).updateStatus(1L, "OCCUPIED");
    }

    @Test
    @WithMockUser
    void deleteSpot_ShouldReturnNoContent() throws Exception {
        doNothing().when(parkingSpotService).deleteSpot(1L);

        mockMvc.perform(delete("/spots/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(parkingSpotService, times(1)).deleteSpot(1L);
    }
}

