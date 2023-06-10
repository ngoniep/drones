package com.musala.drones.dto;


import com.musala.drones.model.Constants;
import com.musala.drones.model.Medication;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DroneDto {


    @Pattern(regexp = "[a-zA-Z0-9 -_]{0,100}",message = "allowed only letters, numbers, ‘-‘, ‘_’")
    String serialNumber;// (100 characters max);
    Constants.DRONE_MODEL model;
    @Min(0)
    @Max(500)
    double weight;// limit (500gr max);
    @Min(0)
    @Max(100)
    int batteryCapacity;// (percentage);
    Constants.DRONE_STATE state;
    //Assuming that a Drone can only carry
    @OneToMany
    @Builder.Default
    Set<MedicationDto> medication=new HashSet<>();
}
