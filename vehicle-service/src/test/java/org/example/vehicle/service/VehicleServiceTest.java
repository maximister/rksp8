package org.example.vehicle.service;

import org.example.vehicle.entity.Vehicle;
import org.example.vehicle.repository.VehicleRepository;
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
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleService vehicleService;

    private Vehicle vehicle1;
    private Vehicle vehicle2;

    @BeforeEach
    void setUp() {
        vehicle1 = new Vehicle(1L, "А123БВ", "Toyota Camry", "Black", 1L);
        vehicle2 = new Vehicle(2L, "В456ГД", "BMW X5", "White", 2L);
    }

    @Test
    void getAllVehicles_ShouldReturnAllVehicles() {
        // Given
        List<Vehicle> vehicles = Arrays.asList(vehicle1, vehicle2);
        when(vehicleRepository.findAll()).thenReturn(vehicles);

        // When
        List<Vehicle> result = vehicleService.getAllVehicles();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(vehicle1, vehicle2);
        verify(vehicleRepository, times(1)).findAll();
    }

    @Test
    void getVehicleById_WhenExists_ShouldReturnVehicle() {
        // Given
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle1));

        // When
        Optional<Vehicle> result = vehicleService.getVehicleById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getPlateNumber()).isEqualTo("А123БВ");
        verify(vehicleRepository, times(1)).findById(1L);
    }

    @Test
    void getVehicleById_WhenNotExists_ShouldReturnEmpty() {
        // Given
        when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Vehicle> result = vehicleService.getVehicleById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(vehicleRepository, times(1)).findById(999L);
    }

    @Test
    void getVehicleByPlateNumber_WhenExists_ShouldReturnVehicle() {
        // Given
        when(vehicleRepository.findByPlateNumber("А123БВ")).thenReturn(Optional.of(vehicle1));

        // When
        Optional<Vehicle> result = vehicleService.getVehicleByPlateNumber("А123БВ");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getPlateNumber()).isEqualTo("А123БВ");
        verify(vehicleRepository, times(1)).findByPlateNumber("А123БВ");
    }

    @Test
    void getVehiclesByOwnerId_ShouldReturnOwnerVehicles() {
        // Given
        List<Vehicle> ownerVehicles = Arrays.asList(vehicle1);
        when(vehicleRepository.findByOwnerId(1L)).thenReturn(ownerVehicles);

        // When
        List<Vehicle> result = vehicleService.getVehiclesByOwnerId(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOwnerId()).isEqualTo(1L);
        verify(vehicleRepository, times(1)).findByOwnerId(1L);
    }

    @Test
    void createVehicle_ShouldSaveAndReturnVehicle() {
        // Given
        Vehicle newVehicle = new Vehicle(null, "С789ЕЖ", "Mercedes C-Class", "Silver", 3L);
        Vehicle savedVehicle = new Vehicle(3L, "С789ЕЖ", "Mercedes C-Class", "Silver", 3L);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(savedVehicle);

        // When
        Vehicle result = vehicleService.createVehicle(newVehicle);

        // Then
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getPlateNumber()).isEqualTo("С789ЕЖ");
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
    }

    @Test
    void updateVehicle_WhenExists_ShouldUpdateAndReturn() {
        // Given
        Vehicle updatedDetails = new Vehicle(null, "А123БВ", "Toyota Camry 2024", "Black", 1L);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle1));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle1);

        // When
        Vehicle result = vehicleService.updateVehicle(1L, updatedDetails);

        // Then
        assertThat(result.getModel()).isEqualTo("Toyota Camry 2024");
        verify(vehicleRepository, times(1)).findById(1L);
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
    }

    @Test
    void updateVehicle_WhenNotExists_ShouldThrowException() {
        // Given
        Vehicle updatedDetails = new Vehicle(null, "А123БВ", "Toyota Camry 2024", "Black", 1L);
        when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> vehicleService.updateVehicle(999L, updatedDetails))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Vehicle not found");
        
        verify(vehicleRepository, times(1)).findById(999L);
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void deleteVehicle_ShouldCallRepository() {
        // Given
        doNothing().when(vehicleRepository).deleteById(1L);

        // When
        vehicleService.deleteVehicle(1L);

        // Then
        verify(vehicleRepository, times(1)).deleteById(1L);
    }
}

