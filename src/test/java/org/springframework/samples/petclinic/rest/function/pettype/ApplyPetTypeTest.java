package org.springframework.samples.petclinic.rest.function.pettype;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.rest.dto.PetTypeDto;

import static org.assertj.core.api.Assertions.assertThat;

class ApplyPetTypeTest {

    @Test
    void copiesNameOntoTheEntity() {
        PetType type = new PetType();
        type.setId(2);
        type.setName("old");

        PetTypeDto request = new PetTypeDto().name("dog");

        new ApplyPetType().service(type, request);

        assertThat(type.getName()).isEqualTo("dog");
        assertThat(type.getId()).isEqualTo(2);
    }
}
