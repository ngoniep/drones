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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

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

    @Autowired
    DroneDispatchRepository droneDispatchRepository;

   public DroneDto registerDrone(DroneDto droneDto){

       Drone drone= droneDtoToDrone(droneDto);
       drone.setState(Constants.DRONE_STATE.IDLE);
       //Add Drone in Database
       return droneToDroneDto(droneRepository.save(drone));
   }

    private DroneDto droneToDroneDto(Drone drone) {
       return DroneDto.builder()
               .weight(drone.getWeight())
               .state(drone.getState())
               .batteryCapacity(drone.getBatteryCapacity())
               .serialNumber(drone.getSerialNumber())
               .model(drone.getModel())
               .medication(droneMedicationService.medicationToMedicationDTO(drone.getMedication(), this))
               .build();
    }

    private Drone droneDtoToDrone(DroneDto droneDto) {
       return  Drone.builder()
               .batteryCapacity(droneDto.getBatteryCapacity())
               .model(droneDto.getModel())
               .medication(droneMedicationService.medicationDtoSetToMedicationSet(droneDto.getMedication(), this))
               .serialNumber(droneDto.getSerialNumber())
               .state(droneDto.getState())
               .weight(droneDto.getWeight())
               .build();
    }

    public ResponseEntity<?> loadDrone(String droneSerialNumber, Set<MedicationDto> medicationDtoList){
       //Prevent the drone from being loaded with more weight that it can carry;
        //Prevent the drone from being in LOADING state if the battery level is below 25%;
        Set<Medication> medicationList= droneMedicationService.medicationDtoSetToMedicationSet(medicationDtoList, this);
        Drone drone=droneRepository.findDroneBySerialNumber(droneSerialNumber).get();//Get The Drone
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

    public ResponseEntity<?>   loadDrone(String serialNumber, MedicationDto medicationDto){
        //Prevent the drone from being loaded with more weight that it can carry;
        //Prevent the drone from being in LOADING state if the battery level is below 25%;

        Medication medication= droneMedicationService.medicationDtoToMedication(medicationDto);

        Drone drone=droneRepository.findDroneBySerialNumber(serialNumber).get();

        if(!(drone.getState().equals(Constants.DRONE_STATE.IDLE)||drone.getState().equals(Constants.DRONE_STATE.LOADING)))
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(ErrorObject.builder()
                    .code("ErrorLoadingDrone")
                    .errorDescription("The drone is not in a state to load")
                    .build());
        if(drone.getBatteryCapacity()<25)
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(ErrorObject.builder()
                    .code("ErrorLoadingDrone")
                    .errorDescription("Please allow the battery to recharge before loading battery capacity is below 25%")
                    .build());
        double currentLoad=0;
        double additionalLoad=medicationDto.getWeight();
        currentLoad = drone.getMedication().stream().mapToDouble(Medication::getWeight).sum();

        //Assuming the load is either accepted in enterity or declined
        if((currentLoad+medication.getWeight())>drone.getWeight()){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(ErrorObject.builder()
                    .code("ErrorLoadingDrone")
                    .errorDescription("The load exceeds the Drone's carrying capacity")
                    .build());
        }

        medication=droneMedicationService.saveMedication(medication);
        drone.getMedication().add(medication);
        if((currentLoad+additionalLoad)<drone.getWeight())
            drone.setState(Constants.DRONE_STATE.LOADING);
        else
            drone.setState(Constants.DRONE_STATE.LOADED);
        drone=droneRepository.save(drone);
        return ResponseEntity.ok(drone);


    }

    public ResponseEntity<?> dispatchDrone(DroneDispatchRequest droneDispatch){
       long timeToDeliver=droneDispatch.getDistance()/droneDispatch.getDroneSpeed();
       DroneDispatch droneDispatch1=null;
        LocalDateTime estimatedTimeOfDelivery = LocalDateTime.now().plusMinutes(timeToDeliver);
       Optional<Drone> drone=droneRepository.findDroneBySerialNumber(droneDispatch.getDroneSerialNumber());
       if(!drone.isPresent())
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorObject.builder()
                .code("ErrorDispatching")
                .errorDescription("The Specified Drone does not exist")
                .build());
       else if(drone.get().getState().equals(Constants.DRONE_STATE.LOADING)||drone.get().getState().equals(Constants.DRONE_STATE.LOADED)){
           Drone d=drone.get();
           d.setState(Constants.DRONE_STATE.DELIVERING);
           droneRepository.save(d);
           droneDispatch1=droneDispatchRepository.save(DroneDispatch.builder()
                   .destination(droneDispatch.getDestination())
                   .distance(droneDispatch.getDistance())
                   .droneSpeed(droneDispatch.getDroneSpeed())
                   .source(droneDispatch.getSource())
                   .estimatedTimeOfDelivery(estimatedTimeOfDelivery)
                   .drone(d)
                   .build());

       }
       else {
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorObject.builder()
                   .code("ErrorDispatching")
                   .errorDescription("Drone in invalid state, Drone should either be loaded or loading to be able to dispatch")
                   .build());
       }
       Map<String,Object> dispatch=new HashMap<>();
       //Get total weight being dispatched
        AtomicReference<Double> totalWeight= new AtomicReference<>((double) 0);
        drone.get().getMedication().forEach(o->{
            totalWeight.updateAndGet(v ->  (v + o.getWeight()));
                });
        dispatch.put("status","Successful");
        dispatch.put("estimatedDeliveryTime",droneDispatch1.getEstimatedTimeOfDelivery());
        dispatch.put("countOfMedications",drone.get().getMedication().size());
        dispatch.put("totalWeight",totalWeight.get());
        dispatch.put("source",droneDispatch.getSource());
        dispatch.put("destination",droneDispatch.getDestination());
       return ResponseEntity.ok(dispatch);
    }

    public ResponseEntity<?> returnDrone(String droneSerialNumber){
        Optional<Drone> drone=droneRepository.findDroneBySerialNumber(droneSerialNumber);
        if(!drone.isPresent())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorObject.builder()
                    .code("ErrorReturningDrone")
                    .errorDescription("The Specified Drone does not exist")
                    .build());
        else if(drone.get().getState().equals(Constants.DRONE_STATE.DELIVERED)){
            Drone d=drone.get();
            d.setState(Constants.DRONE_STATE.RETURNING);
            DroneDispatch droneDispatch=droneDispatchRepository.findByDrone(d);
            droneDispatch.setMedication(d.getMedication());
            d.setMedication(new HashSet<>());
            droneDispatchRepository.save(droneDispatch);
            droneRepository.save(d);

        }
        else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorObject.builder()
                    .code("ErrorReturningDrone")
                    .errorDescription("Drone in invalid state, Drone should be in the DELIVERED state for you to be able to return it")
                    .build());
        }
        Map<String,String> dispatch=new HashMap<>();
        dispatch.put("status","Successful");
        return ResponseEntity.ok(dispatch);
    }
    public ResponseEntity<?>   checkLoadedMedication(String droneSerialNumber){
        Optional<Drone> droneOptional=droneRepository.findDroneBySerialNumber(droneSerialNumber);
        if(!droneOptional.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorObject.builder()
                    .code("ErrorCheckingMedication")
                    .errorDescription("The Drone ID provided is not valid")
                    .build());
        }
        Drone drone=droneOptional.get();
        return ResponseEntity.ok(drone.getMedication());

    }
    public Set<DroneDto> checkDronesByDState(Constants.DRONE_STATE state){
       return droneToDroneDto(droneRepository.findAllByState(state));

    }

    private Set<DroneDto> droneToDroneDto(Set<Drone> allByState) {
       Set<DroneDto> droneDTOs=new HashSet<>();
       for(Drone drone:allByState){
           droneDTOs.add(droneToDroneDto(drone));
       }
       return droneDTOs;
    }

    public ResponseEntity<?> checkBattery(String droneSerialNumber){
        Optional<Drone> droneOptional=droneRepository.findDroneBySerialNumber(droneSerialNumber);
        if(!droneOptional.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorObject.builder()
                    .code("ErrorCheckingMedication")
                    .errorDescription("The Drone ID provided is not valid")
                    .build());
        }
        Drone drone= drone=droneOptional.get();
        return ResponseEntity.ok(drone.getBatteryCapacity());

    }

    public List<Map<String,Object>> getAuditLog(String droneSerialNumber){
        return batteryAuditLogRepository.getAuditForDrone(droneSerialNumber);
    }


    public List<Drone> getAllDrones() {
       return droneRepository.findAll();
    }

    public BatteryAuditLog auditBatteryState(BatteryAuditLog batteryAuditLog) {
      return batteryAuditLogRepository.save(batteryAuditLog);
    }

    public Set<Drone> getDronesByState(List<Constants.DRONE_STATE> filterStates) {
       return droneRepository.findAllByStateIn(filterStates);
    }

    public Drone saveDrone(Drone drone) {
       return droneRepository.save(drone);
    }

    public Set<Drone> getDronesByStateAndMaxBatteryState(List<Constants.DRONE_STATE> statesThatReduceBatter, int i) {
        return droneRepository.findAllByStateInAndBatteryCapacityLessThan(statesThatReduceBatter,100);
    }

    public Set<DroneDispatch> getDispatchesForDrone(Set<Drone> drones) {
       return droneDispatchRepository.findAllByDroneIn(drones);
    }
}
