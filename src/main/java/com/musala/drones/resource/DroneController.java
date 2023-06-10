package com.musala.drones.resource;


import com.musala.drones.dto.DroneDispatchRequest;
import com.musala.drones.dto.DroneDto;
import com.musala.drones.dto.MedicationDto;
import com.musala.drones.model.Constants;
import com.musala.drones.model.Drone;
import com.musala.drones.model.Medication;
import com.musala.drones.service.DroneService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;
import java.util.Set;

@RestController
public class DroneController {
    @Autowired
    DroneService droneService;

    @PostMapping("/register-drone")
    public ResponseEntity<?> registerDrone(@Valid @RequestBody DroneDto drone){

            return ResponseEntity.ok(droneService.registerDrone(drone));

    }

    @PostMapping("/load-medication/{droneId}")
    public ResponseEntity<?> registerDrone(@Valid @RequestBody MedicationDto medication, @PathVariable Long droneId){
            return droneService.loadDrone(droneId,medication);
    }

    @PostMapping("/load-medication-list/{droneId}")
    public ResponseEntity<?> registerDrone(@Valid @RequestBody Set<MedicationDto> medication, @PathVariable Long droneId){
        return droneService.loadDrone(droneId,medication);
    }



    @PostMapping("/dispatch-drone")
    public ResponseEntity<?> registerDrone(@RequestBody DroneDispatchRequest droneDispatchRequest){
        return droneService.dispatchDrone(droneDispatchRequest);
    }

    @GetMapping("/audit-log/{droneId}")
    public ResponseEntity<?> getAuditLog(@PathVariable Long droneId){

        return ResponseEntity.of(Optional.of(droneService.getAuditLog(droneId)));
    }

    @GetMapping("/check-battery/{droneId}")
    public int checkBattery(@PathVariable Long droneId){
        return droneService.checkBattery(droneId);
    }

    @GetMapping("/check-loaded-medication/{droneId}")
    Set<Medication> checkLoadedMedication(@PathVariable Long droneId){
        return droneService.checkLoadedMedication(droneId);
    }

    @GetMapping("/drones-by-state/{state}")
    Set<Drone> checkDronesByDState(@PathVariable Constants.DRONE_STATE state){
        return droneService.checkDronesByDState(state);
    }

}
