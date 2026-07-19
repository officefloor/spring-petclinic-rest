package org.springframework.samples.petclinic.rest.function.specialty;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.repository.SpecialtyRepository;
import org.springframework.samples.petclinic.rest.escalation.NotFoundException;
import org.springframework.samples.petclinic.rest.function.common.Lookups;
import org.springframework.web.bind.annotation.PathVariable;

public class LoadSpecialty {

    public void service(@PathVariable(name = "specialtyId") Integer specialtyId,
            SpecialtyRepository specialtyRepository, Out<Specialty> loaded) throws NotFoundException {
        loaded.set(Lookups.findOrNotFound(() -> specialtyRepository.findById(specialtyId),
                "Specialty not found: " + specialtyId));
    }
}
