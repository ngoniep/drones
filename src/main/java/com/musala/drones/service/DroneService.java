package com.musala.drones.service;

import com.musala.drones.dto.DroneDispatchRequest;
import com.musala.drones.dto.DroneDto;
import com.musala.drones.dto.ErrorObject;
import com.musala.drones.dto.MedicationDto;
import com.musala.drones.model.*;
import com.musala.drones.repository.BatteryAuditLogRepository;
import com.musala.drones.repository.DroneDispatchRepository;
import com.musala.drones.repository.DroneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;

/*registering a drone;
loading a drone with medication items;
checking loaded medication items for a given drone;
checking available drones for loading;
check drone battery level for a given drone;

There is no need for UI;



Introduce a periodic task to check drones battery levels and create history/audit event log for this.*/
@Service
@EnableScheduling
public class DroneService {

    @Autowired
    DroneRepository droneRepository;

   @Autowired
   DroneMedicationService droneMedicationService;

    @Autowired
    BatteryAuditLogRepository batteryAuditLogRepository;

    @Autowired
    DroneDispatchRepository droneDispatchRepository;

   public Drone registerDrone(DroneDto droneDto){

       Drone drone= droneDtoToDto(droneDto);
       drone.setState(Constants.DRONE_STATE.IDLE);
       //Add Drone in Database
       return droneRepository.save(drone);
   }

    private Drone droneDtoToDto(DroneDto droneDto) {
       return  Drone.builder()
               .batteryCapacity(droneDto.getBatteryCapacity())
               .model(droneDto.getModel())
               .medication(medicationDtoSetToMedicationSet(droneDto.getMedication()))
               .serialNumber(droneDto.getSerialNumber())
               .state(droneDto.getState())
               .weight(droneDto.getWeight())
               .build();
    }

    private Set<Medication> medicationDtoSetToMedicationSet(Set<MedicationDto> medications) {
        Set<Medication> medicationSet=new HashSet<>();
        for(MedicationDto medicationDto:medications){
           medicationSet.add(medicationDtoToMedication(medicationDto));
       }
        return medicationSet;
    }

    private Medication medicationDtoToMedication(MedicationDto medicationDto) {
       return Medication.builder()
               .code(medicationDto.getCode())
               .createdTime(medicationDto.getCreatedTime())
               .image(medicationDto.getName())
               .updatedTime(medicationDto.getUpdatedTime())
               .weight(medicationDto.getWeight())
               .name(medicationDto.getName())
               .build();
    }

    public ResponseEntity<?> loadDrone(Long droneId, List<Medication> medicationList){
       //Prevent the drone from being loaded with more weight that it can carry;
        //Prevent the drone from being in LOADING state if the battery level is below 25%;
        Drone drone=droneRepository.findById(droneId).get();//Get The Drone
        if(drone.getBatteryCapacity()<25) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorObject.builder()
                    .code("ErrorLoading")
                    .errorDescription("Battery Capacity is below 25 Drone can not be loaded")
                    .build());
        }
        double currentLoad;
        double additionalLoad;
        currentLoad = drone.getMedication().stream().mapToDouble(Medication::getWeight).sum();
        additionalLoad = medicationList.stream().mapToDouble(Medication::getWeight).sum();

        //Assuming the load is either accepted in enterity or declined
        if((currentLoad+additionalLoad)>drone.getWeight()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorObject.builder()
                    .code("ErrorLoading")
                    .errorDescription("The load is to heavy for the drone! Please pick a different drone or adjust the weight")
                    .build());
        }

        drone.getMedication().addAll(medicationList);
        if((currentLoad+additionalLoad)<drone.getWeight())
           drone.setState(Constants.DRONE_STATE.LOADING);
        else
            drone.setState(Constants.DRONE_STATE.LOADED);
        //Save the new Drone
        return ResponseEntity.ok(drone);


    }

    public ResponseEntity<?>   loadDrone(Long droneId, Medication medication){
        //Prevent the drone from being loaded with more weight that it can carry;
        //Prevent the drone from being in LOADING state if the battery level is below 25%;


        Drone drone=droneRepository.findById(droneId).get();

        if(!(drone.getState().equals(Constants.DRONE_STATE.IDLE)||drone.getState().equals(Constants.DRONE_STATE.LOADING)))
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

    public ResponseEntity<?> dispatchDrone(DroneDispatchRequest droneDispatch){
       Optional<Drone> drone=droneRepository.findById(droneDispatch.getDroneId());
       if(!drone.isPresent())
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorObject.builder()
                .code("ErrorDispatching")
                .errorDescription("The Specified Drone does not exist")
                .build());
       else if(drone.get().getState().equals(Constants.DRONE_STATE.LOADING)||drone.get().getState().equals(Constants.DRONE_STATE.LOADED)){
           Drone d=drone.get();
           d.setState(Constants.DRONE_STATE.DELIVERING);
           droneRepository.save(d);
           droneDispatchRepository.save(DroneDispatch.builder()
                   .destination(droneDispatch.getDestination())
                   .distance(droneDispatch.getDistance())
                   .droneSpeed(droneDispatch.getDroneSpeed())
                   .source(droneDispatch.getSource())
                   .drone(d)
                   .build());

       }
       else {
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorObject.builder()
                   .code("ErrorDispatching")
                   .errorDescription("Drone in invalid state, Drone should either be loaded or loading to be able to dispatch")
                   .build());
       }
       Map<String,String> dispatch=new HashMap<>();
       dispatch.put("status","Successful");
       return ResponseEntity.ok(dispatch);
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



    //log battery state every 5 minutes
    @Scheduled(initialDelay = 0,fixedDelay = 1000*60*5)
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

    //Assuming Drone looses 1% every 2 minutes for testing, in a production environment the drone would be sending these stats
    //Status in a live environment will also be sent via events, the below simulates the same
    @Scheduled(initialDelay = 0,fixedDelay = 1000*60*2)
    public void simulateBatteryDrawDown(){
       //Assuming that the drone will only start running after its delivered and will shut down upon delivery, to only be started on return
       List<Constants.DRONE_STATE> statesThatReduceBatter=new ArrayList<>();
       statesThatReduceBatter.add(Constants.DRONE_STATE.DELIVERING);
        statesThatReduceBatter.add(Constants.DRONE_STATE.RETURNING);
        Set<Drone> drones=droneRepository.findAllByStateInAndAndBatteryCapacityLessThan(statesThatReduceBatter,100);
        for(Drone drone:drones){

             drone.setBatteryCapacity(drone.getBatteryCapacity()-1);
             droneRepository.save(drone);
        }

    }


    @Scheduled(initialDelay = 0,fixedDelay = 1000*60*2)
    public void simulateBatteryRecharge(){
        //Assuming that the drone will only start running after its delivered and will shut down upon delivery, to only be started on return
        List<Constants.DRONE_STATE> statesThatReduceBatter=new ArrayList<>();
        statesThatReduceBatter.add(Constants.DRONE_STATE.IDLE);
        Set<Drone> drones=droneRepository.findAllByStateInAndAndBatteryCapacityLessThan(statesThatReduceBatter,100);
        for(Drone drone:drones){

            drone.setBatteryCapacity(drone.getBatteryCapacity()+1);
            droneRepository.save(drone);
        }

    }



    @Scheduled(initialDelay = 0,fixedDelay = 1000*60*2)
    public void simulateDelivery(){
        //Assuming that the drone will only start running after its delivered and will shut down upon delivery, to only be started on return
        List<Constants.DRONE_STATE> statesThatReduceBatter=new ArrayList<>();
        statesThatReduceBatter.add(Constants.DRONE_STATE.DELIVERING);
        statesThatReduceBatter.add(Constants.DRONE_STATE.RETURNING);
        Set<Drone> drones=droneRepository.findAllByStateInAndAndBatteryCapacityLessThan(statesThatReduceBatter,100);
        Set<DroneDispatch> droneDispatches=droneDispatchRepository.findAllByDroneIn(drones);
        for(DroneDispatch droneDispatch:droneDispatches){
            Drone drone=droneDispatch.getDrone();
            Duration duration = Duration.between(Instant.from(droneDispatch.getStartTime()), Instant.now());
            long timeInFlight=duration.toMinutes();
            if(timeInFlight*droneDispatch.getDroneSpeed()>droneDispatch.getDistance()&&drone.getState().equals(Constants.DRONE_STATE.DELIVERING)){
                drone.setState(Constants.DRONE_STATE.DELIVERED);
            }
            if(timeInFlight*droneDispatch.getDroneSpeed()>droneDispatch.getDistance()&&drone.getState().equals(Constants.DRONE_STATE.RETURNING)){
                drone.setState(Constants.DRONE_STATE.IDLE);
            }
            droneRepository.save(drone);
        }

    }

}
