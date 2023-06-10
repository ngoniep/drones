package com.musala.drones.service;


import com.musala.drones.model.Medication;
import com.musala.drones.repository.MedicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;

@Service
public class DroneMedicationService {
    @Autowired
    MedicationRepository medicationRepository;

    public Medication saveMedication(@Valid Medication medication){
        return medicationRepository.save(medication);
    }

}
