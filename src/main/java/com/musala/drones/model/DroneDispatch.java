package com.musala.drones.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.OneToOne;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DroneDispatch {
    String source;
    String destination;
    int distance;
    @OneToOne
    Drone drone;
    int droneSpeed;
    @CreationTimestamp
    LocalDateTime startTime;
}
