package org.springframework.samples.petclinic.rest.function.specialty;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.repository.SpecialtyRepository;

public class SaveSpecialty {

    public void service(@Val Specialty specialty, SpecialtyRepository specialtyRepository) {
        specialtyRepository.save(specialty);
    }
}
