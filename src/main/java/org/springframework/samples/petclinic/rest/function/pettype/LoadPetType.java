package org.springframework.samples.petclinic.rest.function.pettype;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.repository.PetTypeRepository;
import org.springframework.samples.petclinic.rest.escalation.NotFoundException;
import org.springframework.samples.petclinic.rest.function.common.Lookups;
import org.springframework.web.bind.annotation.PathVariable;

public class LoadPetType {

    public void service(@PathVariable(name = "petTypeId") Integer petTypeId,
            PetTypeRepository petTypeRepository, Out<PetType> loaded) throws NotFoundException {
        loaded.set(Lookups.findOrNotFound(() -> petTypeRepository.findById(petTypeId), "PetType not found: " + petTypeId));
    }
}
