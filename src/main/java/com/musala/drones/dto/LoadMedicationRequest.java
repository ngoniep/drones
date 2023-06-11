package com.musala.drones.dto;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoadMedicationRequest {

    @Pattern(regexp = "[a-zA-Z0-9 -_]*",message = "allowed only letters, numbers, ‘-‘, ‘_’")
    String name;// (allowed only letters, numbers, ‘-‘, ‘_’);
    double weight;
    @Pattern(regexp = "[A-Z0-9_]*",message = "allowed only upper case letters, underscore and numbers")
    String code;// (allowed only upper case letters, underscore and numbers);
    String image;//image (picture of the medication case).
}
