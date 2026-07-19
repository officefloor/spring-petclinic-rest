package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.rest.dto.PetFieldsDto;

public class BuildOwnersPet {

    public void service(@Val Owner owner, @Val PetFieldsDto request, PetMapper petMapper, Out<Pet> built) {
        Pet pet = petMapper.toPet(request);
        pet.setOwner(owner);
        built.set(pet);
    }
}
