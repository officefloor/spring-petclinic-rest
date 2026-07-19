package org.springframework.samples.petclinic.rest.function.vet;

import java.util.List;

import net.officefloor.woof.mock.MockObjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.VetMapperImpl;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.repository.VetRepository;
import org.springframework.samples.petclinic.rest.dto.VetDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListVetsTest {

    @Test
    void respondsOkWithAllVets() {
        Vet vet = new Vet();
        vet.setId(2);
        vet.setFirstName("James");
        vet.setLastName("Carter");
        VetRepository repository = mock(VetRepository.class);
        when(repository.findAll()).thenReturn(List.of(vet));

        MockObjectResponse<ResponseEntity<List<VetDto>>> response = new MockObjectResponse<>();
        new ListVets().service(repository, new VetMapperImpl(), response);

        assertThat(response.getObject().getStatusCode().value()).isEqualTo(200);
        assertThat(response.getObject().getBody()).hasSize(1);
        assertThat(response.getObject().getBody().get(0).getLastName()).isEqualTo("Carter");
    }

    @Test
    void respondsNotFoundWhenEmpty() {
        VetRepository repository = mock(VetRepository.class);
        when(repository.findAll()).thenReturn(List.of());

        MockObjectResponse<ResponseEntity<List<VetDto>>> response = new MockObjectResponse<>();
        new ListVets().service(repository, new VetMapperImpl(), response);

        assertThat(response.getObject().getStatusCode().value()).isEqualTo(404);
    }
}
