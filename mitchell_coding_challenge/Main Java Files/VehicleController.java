package com.joseph.developer.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
public class VehicleController {
    private final int EARLIESTYEAR = 1950;
    private final int LATESTYEAR = 2050;
    @Autowired
    private VehicleRepository vehicleRepository;

    @GetMapping("/vehicles")
    public ResponseEntity<List<Vehicle>> getVehicles(
            @RequestParam(value = "yearBegin", required = false) Integer yearBegin,
            @RequestParam(value =  "yearEnd", required = false) Integer yearEnd,
            @RequestParam(value = "model", required = false) String model,
            @RequestParam(value = "make", required = false) String make )  {

        List<Vehicle> allVehicles = vehicleRepository.findAll();
        Stream<Vehicle> vehiclesStream = allVehicles.stream();

        if(yearBegin==null && yearEnd==null && model==null && make==null) {
            return ResponseEntity.ok().body(allVehicles);
        }
        if (yearBegin != null && yearEnd != null && (yearBegin <= yearEnd)) {
            vehiclesStream = vehiclesStream.filter(v -> (v.getYear() >= yearBegin && v.getYear() <= yearEnd));
        }
        if (model != null) {
            List<String> models = Arrays.asList(model.split(","));
            vehiclesStream = vehiclesStream.filter(v -> models.contains(v.getModel()));
        }
        if (make != null) {
            List<String> makes = Arrays.asList(make.split(","));
            vehiclesStream = vehiclesStream.filter(v -> makes.contains(v.getMake()));
        }

        return ResponseEntity.ok().body(vehiclesStream.collect(Collectors.toList()));
    }

    @GetMapping("/vehicles/{id}")
    public ResponseEntity<Vehicle> getVehiclesById(@PathVariable(value="id") int vehicleId)
            throws ResourceNotFoundException {
        Vehicle vehicle = vehicleRepository
                .findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found on :: " + vehicleId));
        return ResponseEntity.ok().body(vehicle);
    }

    @PostMapping("/vehicles")
    public ResponseEntity createVehicle(@RequestBody Vehicle vehicle) {
        if (vehicle.getId() != 0) {
            vehicle.setId(0);
        }
        if (vehicle.getYear() > LATESTYEAR || vehicle.getYear() < EARLIESTYEAR) {
            return ResponseEntity.badRequest().body("Year out of range. Vehicle was not saved");
        }

        if (vehicle.getMake() == null || vehicle.getMake().length() == 0) {
            return ResponseEntity.badRequest().body("Make was not specified. Vehicle was not saved");
        }

        if (vehicle.getModel() == null || vehicle.getModel().length() == 0) {
            return ResponseEntity.badRequest().body("Model was not specified. Vehicle was not saved");
        }
        vehicleRepository.save(vehicle);
        return ResponseEntity.ok().body(vehicle);
    }

    @PutMapping("/vehicles")
    public ResponseEntity updateVehicle(@RequestBody Vehicle vehicleDetails) throws ResourceNotFoundException {
        Vehicle vehicle =
                vehicleRepository
                        .findById(vehicleDetails.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found on :: "
                                + vehicleDetails.getId()));

        if (vehicleDetails.getYear() < LATESTYEAR && vehicleDetails.getYear() > EARLIESTYEAR) {
            vehicle.setYear(vehicleDetails.getYear());
        } else {
            return ResponseEntity.badRequest().body("Year out of range. Vehicle was not updated");
        }

        if (vehicleDetails.getMake() != null && vehicleDetails.getMake().length() != 0) {
            vehicle.setMake(vehicleDetails.getMake());
        } else {
            return ResponseEntity.badRequest().body("Make was not specified. Vehicle was not updated");
        }

        if (vehicleDetails.getModel() != null && vehicleDetails.getModel().length() != 0) {
            vehicle.setModel(vehicleDetails.getModel());
        } else {
            return ResponseEntity.badRequest().body("Model was not specified. Vehicle was not updated");
        }
        vehicleRepository.save(vehicle);
        return ResponseEntity.ok().body(vehicle);
    }

    @DeleteMapping("/vehicles/{id}")
    public ResponseEntity<String> deleteVehicle(@PathVariable(value = "id") int vehicleId) throws ResourceNotFoundException {
        Vehicle vehicle =
                vehicleRepository
                        .findById(vehicleId)
                        .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found on :: " + vehicleId));
        vehicleRepository.delete(vehicle);
        return ResponseEntity.ok().body("Vehicle successfully deleted");
    }

}
