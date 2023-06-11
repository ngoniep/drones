package com.musala.drones.resource;


import com.musala.drones.dto.*;
import com.musala.drones.model.Constants;
import com.musala.drones.model.Drone;
import com.musala.drones.model.Medication;
import com.musala.drones.service.DroneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RestController
public class DroneController {
    @Autowired
    DroneService droneService;

    @PostMapping("/register-drone")
    public ResponseEntity<?> registerDrone(@Valid @RequestBody DroneRegistrationRequestDto droneRegistrationRequestDto){

        DroneDto drone= DroneDto.builder()
                .batteryCapacity(droneRegistrationRequestDto.getBatteryCapacity())
                .model(droneRegistrationRequestDto.getModel())
                .serialNumber(droneRegistrationRequestDto.getSerialNumber())
                .state(Constants.DRONE_STATE.IDLE)
                .weight(droneRegistrationRequestDto.getWeight())
                .build();
            return ResponseEntity.ok(droneService.registerDrone(drone));
    }

    @GetMapping("/available-drones")
    public ResponseEntity<?> getAvailableDrones(){
        return ResponseEntity.ok(droneService.checkDronesByDState(Constants.DRONE_STATE.IDLE));
    }

    @PostMapping("/load-medication/{droneSerialNumber}")
    public ResponseEntity<?> registerDrone(@Valid @RequestBody LoadMedicationRequest loadMedicationRequest, @PathVariable String droneSerialNumber){
            return droneService.loadDrone(droneSerialNumber, MedicationDto.builder()
                    .name(loadMedicationRequest.getName())
                    .code(loadMedicationRequest.getCode())
                    .image(loadMedicationRequest.getImage())
                    .weight(loadMedicationRequest.getWeight())
                    .build());
    }

    @PostMapping("/load-medication-list/{droneSerialNumber}")
    public ResponseEntity<?> registerDrone(@Valid @RequestBody Set<LoadMedicationRequest> loadMedicationRequests, @PathVariable String droneSerialNumber){
        Set<MedicationDto> medication=new HashSet<>();
        loadMedicationRequests.forEach(loadMedicationRequest->{
            medication.add(MedicationDto.builder()
                    .name(loadMedicationRequest.getName())
                    .code(loadMedicationRequest.getCode())
                    .image(loadMedicationRequest.getImage())
                    .weight(loadMedicationRequest.getWeight())
                    .build());
        });
        return droneService.loadDrone(droneSerialNumber,medication);
    }



    @PostMapping("/dispatch-drone")
    public ResponseEntity<?> registerDrone(@RequestBody DroneDispatchRequest droneDispatchRequest){
        return droneService.dispatchDrone(droneDispatchRequest);
    }

    @GetMapping("/audit-log/{droneSerialNumber}")
    public ResponseEntity<?> getAuditLog(@PathVariable String droneSerialNumber){

        return ResponseEntity.of(Optional.of(droneService.getAuditLog(droneSerialNumber)));
    }

    @GetMapping("/return-drone/{droneSerialNumber}")
    public ResponseEntity<?> returnDrone(@PathVariable String droneSerialNumber){

        return ResponseEntity.of(Optional.of(droneService.returnDrone(droneSerialNumber)));
    }

    @GetMapping("/check-battery/{droneSerialNumber}")
    public ResponseEntity<?> checkBattery(@PathVariable String droneSerialNumber){
        return droneService.checkBattery(droneSerialNumber);
    }

    @GetMapping("/check-loaded-medication/{droneSerialNumber}")
    ResponseEntity<?> checkLoadedMedication(@PathVariable String droneSerialNumber){
        return droneService.checkLoadedMedication(droneSerialNumber);
    }

    @GetMapping("/drones-by-state/{state}")
    Set<DroneDto> checkDronesByDState(@PathVariable Constants.DRONE_STATE state){
        return droneService.checkDronesByDState(state);
    }

}
