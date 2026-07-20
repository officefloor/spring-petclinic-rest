package org.springframework.samples.petclinic.rest.function.pet;

import net.officefloor.woof.mock.MockObjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.PetMapperImpl;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.rest.dto.PetDto;

import static org.assertj.core.api.Assertions.assertThat;

class RespondWithPetUpdatedTest {

    @Test
    void respondsNoContent() {
        Pet pet = new Pet();
        pet.setId(3);
        pet.setName("Rosy I");

        MockObjectResponse<ResponseEntity<PetDto>> response = new MockObjectResponse<>();
        new RespondWithPetUpdated().service(pet, new PetMapperImpl(), response);

        ResponseEntity<PetDto> result = response.getObject();
        assertThat(result.getStatusCode().value()).isEqualTo(204);
        assertThat(result.getBody().getName()).isEqualTo("Rosy I");
    }
}
