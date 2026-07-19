package org.springframework.samples.petclinic.rest.function.pettype;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.rest.dto.PetTypeDto;

public class ApplyPetType {

    public void service(@Val PetType petType, @Val PetTypeDto request) {
        petType.setName(request.getName());
    }
}
