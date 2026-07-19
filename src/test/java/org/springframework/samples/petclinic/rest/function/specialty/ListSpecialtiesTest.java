package org.springframework.samples.petclinic.rest.function.specialty;

import java.util.List;

import net.officefloor.woof.mock.MockObjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.SpecialtyMapperImpl;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.repository.SpecialtyRepository;
import org.springframework.samples.petclinic.rest.dto.SpecialtyDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListSpecialtiesTest {

    @Test
    void respondsOkWithAllSpecialties() {
        Specialty specialty = new Specialty();
        specialty.setId(2);
        specialty.setName("radiology");
        SpecialtyRepository repository = mock(SpecialtyRepository.class);
        when(repository.findAll()).thenReturn(List.of(specialty));

        MockObjectResponse<ResponseEntity<List<SpecialtyDto>>> response = new MockObjectResponse<>();
        new ListSpecialties().service(repository, new SpecialtyMapperImpl(), response);

        assertThat(response.getObject().getStatusCode().value()).isEqualTo(200);
        assertThat(response.getObject().getBody()).hasSize(1);
        assertThat(response.getObject().getBody().get(0).getName()).isEqualTo("radiology");
    }

    @Test
    void respondsNotFoundWhenEmpty() {
        SpecialtyRepository repository = mock(SpecialtyRepository.class);
        when(repository.findAll()).thenReturn(List.of());

        MockObjectResponse<ResponseEntity<List<SpecialtyDto>>> response = new MockObjectResponse<>();
        new ListSpecialties().service(repository, new SpecialtyMapperImpl(), response);

        assertThat(response.getObject().getStatusCode().value()).isEqualTo(404);
    }
}
