package com.musala.drones.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
