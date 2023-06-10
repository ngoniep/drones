package com.musala.drones.repository;

import com.musala.drones.model.Constants;
import com.musala.drones.model.Drone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface DroneRepository extends JpaRepository<Drone,Long> {
    Set<Drone> findAllByState(Constants.DRONE_STATE state);
    Set<Drone> findAllByStateInAndAndBatteryCapacityLessThan(List<Constants.DRONE_STATE> state,int batteryCapacity );
}
