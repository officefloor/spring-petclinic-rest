package org.springframework.samples.petclinic.rest.function.pet;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.mapper.PetMapperImpl;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.rest.dto.PetDto;
import org.springframework.samples.petclinic.rest.dto.PetTypeDto;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ApplyPetDtoTest {

    @Test
    void copiesFieldsOntoTheEntity() {
        Pet pet = new Pet();
        pet.setId(3);
        pet.setName("old");
        pet.setBirthDate(LocalDate.of(2019, 1, 1));
        pet.setType(new PetType());

        PetDto request = new PetDto()
            .name("Rosy I").birthDate(LocalDate.of(2020, 1, 15)).type(new PetTypeDto().id(2).name("dog"));

        new ApplyPetDto().service(pet, request, new PetMapperImpl());

        assertThat(pet.getName()).isEqualTo("Rosy I");
        assertThat(pet.getBirthDate()).isEqualTo(LocalDate.of(2020, 1, 15));
        assertThat(pet.getType().getId()).isEqualTo(2);
        assertThat(pet.getType().getName()).isEqualTo("dog");
        assertThat(pet.getId()).isEqualTo(3);
    }
}
