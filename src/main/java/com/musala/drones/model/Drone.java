package com.musala.drones.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Drone {

    @Id
    @GeneratedValue
    Long id;
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
    Set<Medication> medication=new HashSet<>();
}
