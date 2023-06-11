package com.musala.drones.service;


import com.musala.drones.dto.MedicationDto;
import com.musala.drones.model.Medication;
import com.musala.drones.repository.MedicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.Set;

@Service
public class DroneMedicationService {
    @Autowired
    MedicationRepository medicationRepository;

    public Medication saveMedication(@Valid Medication medication){
        return medicationRepository.save(medication);
    }

    public Set<MedicationDto> medicationToMedicationDTO(Set<Medication> medications, DroneService droneService) {
        Set<MedicationDto> medicationSet=new HashSet<>();
        for(Medication medicationDto:medications){
            medicationSet.add(droneService.droneMedicationService.medicationToMedicationDto(medicationDto));
        }
        return medicationSet;
    }

    public Set<Medication> medicationDtoSetToMedicationSet(Set<MedicationDto> medications, DroneService droneService) {
        Set<Medication> medicationSet=new HashSet<>();
        for(MedicationDto medicationDto:medications){
           medicationSet.add(medicationDtoToMedication(medicationDto));
       }
        return medicationSet;
    }

    public Medication medicationDtoToMedication(MedicationDto medicationDto) {
       return Medication.builder()
               .code(medicationDto.getCode())
               .createdTime(medicationDto.getCreatedTime())
               .image(medicationDto.getImage())
               .updatedTime(medicationDto.getUpdatedTime())
               .weight(medicationDto.getWeight())
               .name(medicationDto.getName())
               .build();
    }

    public MedicationDto medicationToMedicationDto(Medication medication) {
       return MedicationDto.builder()
               .code(medication.getCode())
               .createdTime(medication.getCreatedTime())
               .image(medication.getImage())
               .name(medication.getName())
               .updatedTime(medication.getUpdatedTime())
               .weight(medication.getWeight())
               .build();
    }
}
