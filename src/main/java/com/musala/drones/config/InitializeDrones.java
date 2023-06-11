package com.musala.drones.config;

import com.musala.drones.model.Constants;
import com.musala.drones.model.Drone;
import com.musala.drones.service.DroneMedicationService;
import com.musala.drones.service.DroneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InitializeDrones {

    @Autowired
    DroneMedicationService droneMedicationService;

    @Autowired
    DroneService droneService;

    @Bean
    void initializeTenDrones() {
        for(int i=0;i<10;i++) {
            Drone drone = Drone.builder()
                    .weight((i==0)?248:(int)Math.round(Math.random()*500))
                    .state(Constants.DRONE_STATE.IDLE)
                    .serialNumber((i==0)?"0552-0928-0375-0371":randomSerialNumber())
                    .batteryCapacity((i==0)?98:(int)Math.round(Math.random()*100))
                    .build();
            if (drone.getWeight() < 100) {
                drone.setModel(Constants.DRONE_MODEL.Lightweight);
                droneService.saveDrone(drone);
            } else if (drone.getWeight() < 200) {
                drone.setModel(Constants.DRONE_MODEL.Middleweight);
                droneService.saveDrone(drone);
            } else if (drone.getWeight() < 300) {
                drone.setModel(Constants.DRONE_MODEL.Cruiserweight);
                droneService.saveDrone(drone);
            } else {
                drone.setModel(Constants.DRONE_MODEL.Heavyweight);
                droneService.saveDrone(drone);
            }
        }
    }

    String randomSerialNumber(){
        return randomFourDigit()+"-"+randomFourDigit()+"-"+randomFourDigit()+"-"+randomFourDigit();
    }
    String randomFourDigit(){
        String s=""+Math.round(Math.random()*1000);
        if(s.length()<4)
            s="0"+s;
        return s;
    }
    double getRandomWeight(){
        return Math.round(Math.random()*500);
    }

}
