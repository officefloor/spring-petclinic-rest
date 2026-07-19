package org.springframework.samples.petclinic.rest.function.pettype;

import net.officefloor.woof.mock.MockObjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.PetTypeMapperImpl;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.rest.dto.PetTypeDto;

import static org.assertj.core.api.Assertions.assertThat;

class RespondWithPetTypeCreatedTest {

    @Test
    void respondsCreatedWithLocationAndBody() {
        PetType type = new PetType();
        type.setId(9);
        type.setName("dog");

        MockObjectResponse<ResponseEntity<PetTypeDto>> response = new MockObjectResponse<>();
        new RespondWithPetTypeCreated().service(type, new PetTypeMapperImpl(), response);

        ResponseEntity<PetTypeDto> result = response.getObject();
        assertThat(result.getStatusCode().value()).isEqualTo(201);
        assertThat(result.getHeaders().getLocation().toString()).isEqualTo("/api/pettypes/9");
        assertThat(result.getBody().getName()).isEqualTo("dog");
    }
}
