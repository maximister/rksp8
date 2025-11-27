package org.example.vehicle.repository;

import org.example.vehicle.entity.Vehicle;
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
class VehicleRepositoryTest {

    @Autowired
    private VehicleRepository vehicleRepository;

    private Vehicle vehicle1;
    private Vehicle vehicle2;
    private Vehicle vehicle3;

    @BeforeEach
    void setUp() {
        vehicleRepository.deleteAll();
        
        vehicle1 = new Vehicle(null, "А123БВ", "Toyota Camry", "Black", 1L);
        vehicle2 = new Vehicle(null, "В456ГД", "BMW X5", "White", 2L);
        vehicle3 = new Vehicle(null, "С789ЕЖ", "Mercedes C-Class", "Silver", 1L);

        vehicleRepository.save(vehicle1);
        vehicleRepository.save(vehicle2);
        vehicleRepository.save(vehicle3);
    }

    @Test
    void findAll_ShouldReturnAllVehicles() {
        // When
        List<Vehicle> vehicles = vehicleRepository.findAll();

        // Then
        assertThat(vehicles).hasSize(3);
    }

    @Test
    void findById_WhenExists_ShouldReturnVehicle() {
        // Given
        Long savedId = vehicle1.getId();

        // When
        Optional<Vehicle> found = vehicleRepository.findById(savedId);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getPlateNumber()).isEqualTo("А123БВ");
    }

    @Test
    void findById_WhenNotExists_ShouldReturnEmpty() {
        // When
        Optional<Vehicle> found = vehicleRepository.findById(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByPlateNumber_WhenExists_ShouldReturnVehicle() {
        // When
        Optional<Vehicle> found = vehicleRepository.findByPlateNumber("А123БВ");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getPlateNumber()).isEqualTo("А123БВ");
        assertThat(found.get().getModel()).isEqualTo("Toyota Camry");
    }

    @Test
    void findByPlateNumber_WhenNotExists_ShouldReturnEmpty() {
        // When
        Optional<Vehicle> found = vehicleRepository.findByPlateNumber("Х999ХХ");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByOwnerId_ShouldReturnOwnerVehicles() {
        // When
        List<Vehicle> ownerVehicles = vehicleRepository.findByOwnerId(1L);

        // Then
        assertThat(ownerVehicles).hasSize(2);
        assertThat(ownerVehicles).allMatch(v -> v.getOwnerId().equals(1L));
    }

    @Test
    void findByOwnerId_WhenNoVehicles_ShouldReturnEmpty() {
        // When
        List<Vehicle> ownerVehicles = vehicleRepository.findByOwnerId(999L);

        // Then
        assertThat(ownerVehicles).isEmpty();
    }

    @Test
    void save_ShouldPersistNewVehicle() {
        // Given
        Vehicle newVehicle = new Vehicle(null, "Д111АА", "Audi A4", "Blue", 3L);

        // When
        Vehicle saved = vehicleRepository.save(newVehicle);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getPlateNumber()).isEqualTo("Д111АА");
        assertThat(vehicleRepository.findAll()).hasSize(4);
    }

    @Test
    void save_ShouldUpdateExistingVehicle() {
        // Given
        Long vehicleId = vehicle1.getId();
        vehicle1.setModel("Toyota Camry 2024");

        // When
        Vehicle updated = vehicleRepository.save(vehicle1);

        // Then
        assertThat(updated.getId()).isEqualTo(vehicleId);
        assertThat(updated.getModel()).isEqualTo("Toyota Camry 2024");
    }

    @Test
    void deleteById_ShouldRemoveVehicle() {
        // Given
        Long vehicleId = vehicle1.getId();
        assertThat(vehicleRepository.findById(vehicleId)).isPresent();

        // When
        vehicleRepository.deleteById(vehicleId);

        // Then
        assertThat(vehicleRepository.findById(vehicleId)).isEmpty();
        assertThat(vehicleRepository.findAll()).hasSize(2);
    }
}

