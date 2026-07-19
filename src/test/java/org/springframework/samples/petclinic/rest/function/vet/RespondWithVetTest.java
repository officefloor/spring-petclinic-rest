package org.springframework.samples.petclinic.rest.function.vet;

import net.officefloor.woof.mock.MockObjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.mapper.VetMapperImpl;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.rest.dto.VetDto;

import static org.assertj.core.api.Assertions.assertThat;

class RespondWithVetTest {

    @Test
    void mapsEntityToDto() {
        Vet vet = new Vet();
        vet.setId(2);
        vet.setFirstName("James");
        vet.setLastName("Carter");

        MockObjectResponse<VetDto> response = new MockObjectResponse<>();
        new RespondWithVet().service(vet, new VetMapperImpl(), response);

        assertThat(response.getObject().getId()).isEqualTo(2);
        assertThat(response.getObject().getLastName()).isEqualTo("Carter");
    }
}
