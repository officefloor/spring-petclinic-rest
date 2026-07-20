package org.springframework.samples.petclinic.rest.function.pet;

import java.util.List;

import net.officefloor.woof.mock.MockObjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.PetMapperImpl;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.repository.PetRepository;
import org.springframework.samples.petclinic.rest.dto.PetDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListPetsTest {

    @Test
    void respondsOkWithAllPets() {
        Pet pet = new Pet();
        pet.setId(3);
        pet.setName("Rosy");
        PetRepository repository = mock(PetRepository.class);
        when(repository.findAll()).thenReturn(List.of(pet));

        MockObjectResponse<ResponseEntity<List<PetDto>>> response = new MockObjectResponse<>();
        new ListPets().service(repository, new PetMapperImpl(), response);

        assertThat(response.getObject().getStatusCode().value()).isEqualTo(200);
        assertThat(response.getObject().getBody()).hasSize(1);
        assertThat(response.getObject().getBody().get(0).getName()).isEqualTo("Rosy");
    }

    @Test
    void respondsNotFoundWhenEmpty() {
        PetRepository repository = mock(PetRepository.class);
        when(repository.findAll()).thenReturn(List.of());

        MockObjectResponse<ResponseEntity<List<PetDto>>> response = new MockObjectResponse<>();
        new ListPets().service(repository, new PetMapperImpl(), response);

        assertThat(response.getObject().getStatusCode().value()).isEqualTo(404);
    }
}
