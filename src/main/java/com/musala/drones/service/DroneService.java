package com.musala.drones.service;

import com.musala.drones.model.BatteryAuditLog;
import com.musala.drones.model.Constants;
import com.musala.drones.model.Drone;
import com.musala.drones.model.Medication;
import com.musala.drones.repository.BatteryAuditLogRepository;
import com.musala.drones.repository.DroneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/*registering a drone;
loading a drone with medication items;
checking loaded medication items for a given drone;
checking available drones for loading;
check drone battery level for a given drone;

There is no need for UI;



Introduce a periodic task to check drones battery levels and create history/audit event log for this.*/
@Service
public class DroneService {

    @Autowired
    DroneRepository droneRepository;

   @Autowired
   DroneMedicationService droneMedicationService;

    @Autowired
    BatteryAuditLogRepository batteryAuditLogRepository;
   public Drone registerDrone(Drone drone){

       //Add Drone in Database
       return droneRepository.save(drone);
   }

    public Drone loadDrone(Long droneId, List<Medication> medicationList){
       //Prevent the drone from being loaded with more weight that it can carry;
        //Prevent the drone from being in LOADING state if the battery level is below 25%;
        Drone drone=droneRepository.findById(droneId).get();//Get The Drone
        if(drone.getBatteryCapacity()<25) {
            return null;
        }
        double currentLoad;
        double additionalLoad;
        currentLoad = drone.getMedication().stream().mapToDouble(Medication::getWeight).sum();
        additionalLoad = medicationList.stream().mapToDouble(Medication::getWeight).sum();

        //Assuming the load is either accepted in enterity or declined
        if((currentLoad+additionalLoad)>drone.getWeight()){
            return null;
        }

        drone.getMedication().addAll(medicationList);
        //Save the new Drone
        return drone;


    }

    public ResponseEntity<?>   loadDrone(Long droneId, Medication medication){
        //Prevent the drone from being loaded with more weight that it can carry;
        //Prevent the drone from being in LOADING state if the battery level is below 25%;


        Drone drone=droneRepository.findById(droneId).get();

        if(!drone.getState().equals(Constants.DRONE_STATE.IDLE))
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body("The drone is not in a state to load");
        double currentLoad;
        double additionalLoad;
        currentLoad = drone.getMedication().stream().mapToDouble(Medication::getWeight).sum();

        //Assuming the load is either accepted in enterity or declined
        if((currentLoad+medication.getWeight())>drone.getWeight()){
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body("The load exceeds the Drone's carrying capacity");
        }

        medication=droneMedicationService.saveMedication(medication);
        drone.getMedication().add(medication);
        drone.setState(Constants.DRONE_STATE.LOADED);
        drone=droneRepository.save(drone);
        return ResponseEntity.ok(drone);


    }

    public Set<Medication>   checkLoadedMedication(Long droneId){
        Drone drone=droneRepository.findById(droneId).get();
        return drone.getMedication();

    }
    public Set<Drone> checkDronesByDState(Constants.DRONE_STATE state){
       return droneRepository.findAllByState(state);

    }

    public int checkBattery(Long dronId){
      return droneRepository.findById(dronId).get().getBatteryCapacity();
    }

    public List<BatteryAuditLog> getAuditLog(Long dronId){
        return batteryAuditLogRepository.findAll();
    }



    //log battery state every 30 minutes
    @Scheduled(initialDelay = 0,fixedDelay = 1000*60)
    public void logBatteryState(){
       List<Drone> drones=droneRepository.findAll();
       for(Drone drone:drones){
           BatteryAuditLog batteryAuditLog= BatteryAuditLog.builder()
                   .batteryPercentage(drone.getBatteryCapacity())
                   .drone(drone)
                   .build();
           batteryAuditLogRepository.save(batteryAuditLog);
       }

    }

}
