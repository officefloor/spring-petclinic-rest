package org.springframework.samples.petclinic.rest.function.pet;

import net.officefloor.woof.mock.MockObjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.PetMapperImpl;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.rest.dto.PetDto;

import static org.assertj.core.api.Assertions.assertThat;

class RespondWithPetCreatedTest {

    @Test
    void respondsCreatedWithLocationAndBody() {
        Pet pet = new Pet();
        pet.setId(9);
        pet.setName("Rosy");

        MockObjectResponse<ResponseEntity<PetDto>> response = new MockObjectResponse<>();
        new RespondWithPetCreated().service(pet, new PetMapperImpl(), response);

        ResponseEntity<PetDto> result = response.getObject();
        assertThat(result.getStatusCode().value()).isEqualTo(201);
        assertThat(result.getHeaders().getLocation().toString()).isEqualTo("/api/pets/9");
        assertThat(result.getBody().getName()).isEqualTo("Rosy");
    }
}
