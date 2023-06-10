package com.musala.drones.dto;

import com.musala.drones.model.Medication;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DroneDispatchRequest {
    String source;
    String destination;
    int distance;
    Long droneId;
    int droneSpeed;
    //Assuming that a Drone can only carry
}
