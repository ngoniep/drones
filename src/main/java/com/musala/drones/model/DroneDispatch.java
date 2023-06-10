package com.musala.drones.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.time.LocalDateTime;

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
    @CreationTimestamp
    LocalDateTime startTime;
}
