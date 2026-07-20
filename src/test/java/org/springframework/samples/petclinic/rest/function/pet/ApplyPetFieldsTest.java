package org.springframework.samples.petclinic.rest.function.pet;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.mapper.PetMapperImpl;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.rest.dto.PetFieldsDto;
import org.springframework.samples.petclinic.rest.dto.PetTypeDto;

import static org.assertj.core.api.Assertions.assertThat;

class ApplyPetFieldsTest {

    @Test
    void copiesFieldsOntoTheEntity() {
        Pet pet = new Pet();
        pet.setName("old");
        pet.setType(new PetType());

        PetFieldsDto request = new PetFieldsDto();
        request.setName("Rex");
        request.setBirthDate(LocalDate.of(2020, 1, 15));
        request.setType(new PetTypeDto().id(2).name("dog"));

        new ApplyPetFields().service(pet, request, new PetMapperImpl());

        assertThat(pet.getName()).isEqualTo("Rex");
        assertThat(pet.getBirthDate()).isEqualTo(LocalDate.of(2020, 1, 15));
        assertThat(pet.getType().getId()).isEqualTo(2);
    }
}
