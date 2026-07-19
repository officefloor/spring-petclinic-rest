package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

import net.officefloor.woof.mock.MockObjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListOwnersTest {

    @Test
    void respondsNotFoundWhenEmpty() {
        OwnerRepository repository = mock(OwnerRepository.class);
        when(repository.findAll()).thenReturn(List.of());

        MockObjectResponse<ResponseEntity<List<OwnerDto>>> response = new MockObjectResponse<>();
        new ListOwners().service(null, repository, mock(OwnerMapper.class), response);

        assertThat(response.getObject().getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void filtersByLastNameWhenProvided() {
        Owner owner = new Owner();
        owner.setLastName("Davis");
        OwnerRepository repository = mock(OwnerRepository.class);
        when(repository.findByLastName("Davis")).thenReturn(List.of(owner));
        OwnerMapper mapper = mock(OwnerMapper.class);
        OwnerDto dto = new OwnerDto();
        when(mapper.toOwnerDtoCollection(List.of(owner))).thenReturn(List.of(dto));

        MockObjectResponse<ResponseEntity<List<OwnerDto>>> response = new MockObjectResponse<>();
        new ListOwners().service("Davis", repository, mapper, response);

        assertThat(response.getObject().getBody()).containsExactly(dto);
    }
}
