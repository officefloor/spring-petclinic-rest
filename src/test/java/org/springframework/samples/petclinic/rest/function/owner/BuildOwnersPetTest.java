package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.model.test.variable.MockVar;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.mapper.PetMapperImpl;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.rest.dto.PetFieldsDto;
import org.springframework.samples.petclinic.rest.dto.PetTypeDto;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class BuildOwnersPetTest {

    @Test
    void buildsPetAssociatedWithOwner() {
        Owner owner = new Owner();
        owner.setId(7);

        PetFieldsDto request = new PetFieldsDto();
        request.setName("Rosy");
        request.setBirthDate(LocalDate.of(2020, 1, 15));
        request.setType(new PetTypeDto().id(2).name("dog"));

        MockVar<Pet> built = new MockVar<>();
        new BuildOwnersPet().service(owner, request, new PetMapperImpl(), built);

        Pet pet = built.get();
        assertEquals("Rosy", pet.getName());
        assertEquals(LocalDate.of(2020, 1, 15), pet.getBirthDate());
        assertSame(owner, pet.getOwner());
    }
}
