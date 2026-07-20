package org.springframework.samples.petclinic.rest.function.vet;

import net.officefloor.woof.mock.MockObjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.VetMapperImpl;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.rest.dto.VetDto;

import static org.assertj.core.api.Assertions.assertThat;

class RespondWithVetUpdatedTest {

    @Test
    void respondsNoContent() {
        Vet vet = new Vet();
        vet.setId(2);
        vet.setFirstName("James");
        vet.setLastName("Carter");

        MockObjectResponse<ResponseEntity<VetDto>> response = new MockObjectResponse<>();
        new RespondWithVetUpdated().service(vet, new VetMapperImpl(), response);

        ResponseEntity<VetDto> result = response.getObject();
        assertThat(result.getStatusCode().value()).isEqualTo(204);
        assertThat(result.getBody().getLastName()).isEqualTo("Carter");
    }
}
