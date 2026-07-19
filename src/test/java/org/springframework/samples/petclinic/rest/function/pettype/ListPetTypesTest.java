package org.springframework.samples.petclinic.rest.function.pettype;

import java.util.List;

import net.officefloor.woof.mock.MockObjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.PetTypeMapperImpl;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.repository.PetTypeRepository;
import org.springframework.samples.petclinic.rest.dto.PetTypeDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListPetTypesTest {

    @Test
    void respondsOkWithAllPetTypes() {
        PetType type = new PetType();
        type.setId(2);
        type.setName("dog");
        PetTypeRepository repository = mock(PetTypeRepository.class);
        when(repository.findAll()).thenReturn(List.of(type));

        MockObjectResponse<ResponseEntity<List<PetTypeDto>>> response = new MockObjectResponse<>();
        new ListPetTypes().service(repository, new PetTypeMapperImpl(), response);

        assertThat(response.getObject().getStatusCode().value()).isEqualTo(200);
        assertThat(response.getObject().getBody()).hasSize(1);
        assertThat(response.getObject().getBody().get(0).getName()).isEqualTo("dog");
    }

    @Test
    void respondsNotFoundWhenEmpty() {
        PetTypeRepository repository = mock(PetTypeRepository.class);
        when(repository.findAll()).thenReturn(List.of());

        MockObjectResponse<ResponseEntity<List<PetTypeDto>>> response = new MockObjectResponse<>();
        new ListPetTypes().service(repository, new PetTypeMapperImpl(), response);

        assertThat(response.getObject().getStatusCode().value()).isEqualTo(404);
    }
}
