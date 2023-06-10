package com.musala.drones.repository;

import com.musala.drones.model.Drone;
import com.musala.drones.model.DroneDispatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface DroneDispatchRepository extends JpaRepository<DroneDispatch,Long> {
    Set<DroneDispatch> findAllByDroneIn(Set<Drone> drones);
}
