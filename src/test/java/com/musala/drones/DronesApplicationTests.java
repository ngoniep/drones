package com.musala.drones;

import com.musala.drones.model.Constants;
import com.musala.drones.model.Drone;
import com.musala.drones.model.Medication;
import com.musala.drones.service.DroneMedicationService;
import com.musala.drones.service.DroneService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DronesApplicationTests {

	@Autowired
	DroneMedicationService droneMedicationService;

	@Autowired
	DroneService droneService;

	@Test
	void contextLoads() {
	}

}
