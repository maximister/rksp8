package org.example.vehicle.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.vehicle.entity.Vehicle;
import org.example.vehicle.service.VehicleService;
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

@WebMvcTest(VehicleController.class)
@ActiveProfiles("test")
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VehicleService vehicleService;

    private Vehicle vehicle1;
    private Vehicle vehicle2;

    @BeforeEach
    void setUp() {
        vehicle1 = new Vehicle(1L, "А123БВ", "Toyota Camry", "Black", 1L);
        vehicle2 = new Vehicle(2L, "В456ГД", "BMW X5", "White", 2L);
    }

    @Test
    @WithMockUser
    void getAllVehicles_ShouldReturnListOfVehicles() throws Exception {
        List<Vehicle> vehicles = Arrays.asList(vehicle1, vehicle2);
        when(vehicleService.getAllVehicles()).thenReturn(vehicles);

        mockMvc.perform(get("/vehicles"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].plateNumber").value("А123БВ"))
                .andExpect(jsonPath("$[1].plateNumber").value("В456ГД"));

        verify(vehicleService, times(1)).getAllVehicles();
    }

    @Test
    @WithMockUser
    void getVehicleById_WhenExists_ShouldReturnVehicle() throws Exception {
        when(vehicleService.getVehicleById(1L)).thenReturn(Optional.of(vehicle1));

        mockMvc.perform(get("/vehicles/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.plateNumber").value("А123БВ"))
                .andExpect(jsonPath("$.model").value("Toyota Camry"));

        verify(vehicleService, times(1)).getVehicleById(1L);
    }

    @Test
    @WithMockUser
    void getVehicleById_WhenNotExists_ShouldReturn404() throws Exception {
        when(vehicleService.getVehicleById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/vehicles/999"))
                .andExpect(status().isNotFound());

        verify(vehicleService, times(1)).getVehicleById(999L);
    }

    @Test
    @WithMockUser
    void getVehicleByPlateNumber_WhenExists_ShouldReturnVehicle() throws Exception {
        when(vehicleService.getVehicleByPlateNumber("А123БВ")).thenReturn(Optional.of(vehicle1));

        mockMvc.perform(get("/vehicles/plate/А123БВ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plateNumber").value("А123БВ"));

        verify(vehicleService, times(1)).getVehicleByPlateNumber("А123БВ");
    }

    @Test
    @WithMockUser
    void getVehiclesByOwnerId_ShouldReturnOwnerVehicles() throws Exception {
        List<Vehicle> ownerVehicles = Arrays.asList(vehicle1);
        when(vehicleService.getVehiclesByOwnerId(1L)).thenReturn(ownerVehicles);

        mockMvc.perform(get("/vehicles/owner/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ownerId").value(1));

        verify(vehicleService, times(1)).getVehiclesByOwnerId(1L);
    }

    @Test
    @WithMockUser
    void createVehicle_ShouldReturnCreatedVehicle() throws Exception {
        Vehicle newVehicle = new Vehicle(null, "С789ЕЖ", "Mercedes C-Class", "Silver", 3L);
        Vehicle savedVehicle = new Vehicle(3L, "С789ЕЖ", "Mercedes C-Class", "Silver", 3L);
        
        when(vehicleService.createVehicle(any(Vehicle.class))).thenReturn(savedVehicle);

        mockMvc.perform(post("/vehicles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newVehicle)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.plateNumber").value("С789ЕЖ"));

        verify(vehicleService, times(1)).createVehicle(any(Vehicle.class));
    }

    @Test
    @WithMockUser
    void updateVehicle_ShouldReturnUpdatedVehicle() throws Exception {
        Vehicle updatedVehicle = new Vehicle(1L, "А123БВ", "Toyota Camry 2024", "Black", 1L);
        
        when(vehicleService.updateVehicle(eq(1L), any(Vehicle.class))).thenReturn(updatedVehicle);

        mockMvc.perform(put("/vehicles/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedVehicle)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("Toyota Camry 2024"));

        verify(vehicleService, times(1)).updateVehicle(eq(1L), any(Vehicle.class));
    }

    @Test
    @WithMockUser
    void deleteVehicle_ShouldReturnNoContent() throws Exception {
        doNothing().when(vehicleService).deleteVehicle(1L);

        mockMvc.perform(delete("/vehicles/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(vehicleService, times(1)).deleteVehicle(1L);
    }
}

