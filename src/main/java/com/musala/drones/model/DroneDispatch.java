package com.musala.drones.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class DroneDispatch {
    @Id
    @GeneratedValue
    Long id;
    String source;
    String destination;
    int distance;
    @OneToOne
    Drone drone;
    int droneSpeed;
    @Builder.Default
    LocalTime startTime=LocalTime.now();
    LocalTime estimatedTimeOfDelivery;
    //Assuming that a Drone can only carry
    @OneToMany(fetch = FetchType.EAGER)
    @Builder.Default
    Set<Medication> medication=new HashSet<>();
}
