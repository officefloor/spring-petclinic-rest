package org.springframework.samples.petclinic.rest.function.pettype;

import net.officefloor.woof.mock.MockObjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.mapper.PetTypeMapperImpl;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.rest.dto.PetTypeDto;

import static org.assertj.core.api.Assertions.assertThat;

class RespondWithPetTypeTest {

    @Test
    void mapsEntityToDto() {
        PetType type = new PetType();
        type.setId(2);
        type.setName("dog");

        MockObjectResponse<PetTypeDto> response = new MockObjectResponse<>();
        new RespondWithPetType().service(type, new PetTypeMapperImpl(), response);

        assertThat(response.getObject().getId()).isEqualTo(2);
        assertThat(response.getObject().getName()).isEqualTo("dog");
    }
}
