package com.musala.drones.resource;


import com.musala.drones.model.Drone;
import com.musala.drones.model.Medication;
import com.musala.drones.service.DroneService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class DroneController {
    @Autowired
    DroneService droneService;

    @PostMapping("/register-drone")
    public ResponseEntity<?> registerDrone(@Valid @RequestBody Drone drone){

            return ResponseEntity.ok(droneService.registerDrone(drone));

    }

    @PostMapping("/load-medication/{droneId}")
    public ResponseEntity<?> registerDrone(@Valid @RequestBody Medication medication, @PathVariable Long droneId){

            return droneService.loadDrone(droneId,medication);

    }
}
