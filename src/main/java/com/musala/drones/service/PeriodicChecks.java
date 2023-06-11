package com.musala.drones.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.musala.drones.model.BatteryAuditLog;
import com.musala.drones.model.Constants;
import com.musala.drones.model.Drone;
import com.musala.drones.model.DroneDispatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@EnableScheduling
@Service
public class PeriodicChecks {

    @Autowired
    DroneService droneService;

    //log battery state every 5 minutes
    @Scheduled(initialDelay = 0,fixedDelay = 1000*30)
    public void logBatteryState(){
        List<Constants.DRONE_STATE> statesThatReduceBatter=new ArrayList<>();
        statesThatReduceBatter.add(Constants.DRONE_STATE.DELIVERING);
        statesThatReduceBatter.add(Constants.DRONE_STATE.RETURNING);
        Set<Drone> drones=droneService.getDronesByState(statesThatReduceBatter);
        for(Drone drone:drones){
            BatteryAuditLog batteryAuditLog= BatteryAuditLog.builder()
                    .batteryPercentage(drone.getBatteryCapacity())
                    .drone(drone)
                    .build();
            droneService.auditBatteryState(batteryAuditLog);
        }

    }

    //Assuming Drone looses 1% every 18 seconds for testing, in a production environment the drone would be sending these stats
    //Status in a live environment will also be sent via events, the below simulates the same
    @Scheduled(initialDelay = 0,fixedDelay = 1000*10)
    public void simulateBatteryDrawDown(){
        //Assuming that the drone will only start running after its delivered and will shut down upon delivery, to only be started on return
        List<Constants.DRONE_STATE> statesThatReduceBatter=new ArrayList<>();
        statesThatReduceBatter.add(Constants.DRONE_STATE.DELIVERING);
        statesThatReduceBatter.add(Constants.DRONE_STATE.RETURNING);
        Set<Drone> drones=droneService.getDronesByState(statesThatReduceBatter);
        for(Drone drone:drones){
            drone.setBatteryCapacity(drone.getBatteryCapacity()-1);
            if(drone.getBatteryCapacity()==25){
                Logger.getLogger("Battery Draw Down Simulator ").log(Level.WARNING,"Drone "+drone.getSerialNumber()+" Battery is now critical");
            }
            droneService.saveDrone(drone);
        }

    }


    @Scheduled(initialDelay = 0,fixedDelay = 1000*15)
    public void simulateBatteryRecharge() throws JsonProcessingException {
        //Assuming that the drone will only start running after its delivered and will shut down upon delivery, to only be started on return
        List<Constants.DRONE_STATE> statesThatReduceBatter=new ArrayList<>();
        statesThatReduceBatter.add(Constants.DRONE_STATE.IDLE);
        Map<String,Integer> batteryCapacities=new HashMap<>();
        Set<Drone> drones=droneService.getDronesByStateAndMaxBatteryState(statesThatReduceBatter,100);
        for(Drone drone:drones){

            drone.setBatteryCapacity(drone.getBatteryCapacity()+1);
            batteryCapacities.put(drone.getSerialNumber(),drone.getBatteryCapacity());
            droneService.saveDrone(drone);
            if(drone.getBatteryCapacity()==100){
                batteryCapacities.remove(drone.getSerialNumber());
                Logger.getLogger("Battery Recharge Simulation").log(Level.INFO,"Drone "+drone.getSerialNumber()+" is fully recharge will stop recharging");
            }
        }
        if(!batteryCapacities.isEmpty())
            Logger.getLogger("Battery Recharge Simulation").log(Level.INFO,"Recharging batteries "+ new ObjectMapper().writeValueAsString(batteryCapacities));

    }



    @Scheduled(initialDelay = 0,fixedDelay = 1000*60)
    public void simulateDelivery(){
        //Assuming that the drone will only start running after its delivered and will shut down upon delivery, to only be started on return
        List<Constants.DRONE_STATE> statesThatReduceBatter=new ArrayList<>();
        statesThatReduceBatter.add(Constants.DRONE_STATE.DELIVERING);
        statesThatReduceBatter.add(Constants.DRONE_STATE.RETURNING);
        Set<Drone> drones=droneService.getDronesByState(statesThatReduceBatter);
        Set<DroneDispatch> droneDispatches=droneService.getDispatchesForDrone(drones);
        for(DroneDispatch droneDispatch:droneDispatches){
            Drone drone=droneDispatch.getDrone();
            Duration duration = Duration.between(droneDispatch.getStartTime().toLocalTime(), LocalTime.now());
            long timeInFlight=Math.abs(duration.toMinutes());
            if(timeInFlight*droneDispatch.getDroneSpeed()>=droneDispatch.getDistance()&&drone.getState().equals(Constants.DRONE_STATE.DELIVERING)){
                drone.setState(Constants.DRONE_STATE.DELIVERED);
                Logger.getAnonymousLogger().log(Level.INFO,"Drone "+drone.getSerialNumber()+" has has arrived with delivery at a delivery at "+droneDispatch.getDestination(),drone.getSerialNumber()+" actual time of delivery "+LocalTime.now()+" estimated time of delivery "+droneDispatch.getEstimatedTimeOfDelivery());
            }
            if(timeInFlight*droneDispatch.getDroneSpeed()>=droneDispatch.getDistance()&&drone.getState().equals(Constants.DRONE_STATE.RETURNING)){
                drone.setState(Constants.DRONE_STATE.IDLE);
                Logger.getAnonymousLogger().log(Level.INFO,"Drone "+drone.getSerialNumber()+" returned "+droneDispatch.getDestination()+" battery charge on return is "+drone.getBatteryCapacity()+" battery will start to recharge now");
            }
            droneService.saveDrone(drone);
        }
        Logger.getLogger("Battery Recharge Simulation").log(Level.INFO,"Recharging batteries ");


    }
}
