package org.springframework.samples.petclinic.rest.function.visit;

import java.time.LocalDate;
import java.util.List;

import net.officefloor.woof.mock.MockObjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.VisitMapperImpl;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.repository.VisitRepository;
import org.springframework.samples.petclinic.rest.dto.VisitDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListVisitsTest {

    @Test
    void respondsOkWithAllVisits() {
        Visit visit = new Visit();
        visit.setId(2);
        visit.setDate(LocalDate.now());
        visit.setDescription("rabies shot");
        VisitRepository repository = mock(VisitRepository.class);
        when(repository.findAll()).thenReturn(List.of(visit));

        MockObjectResponse<ResponseEntity<List<VisitDto>>> response = new MockObjectResponse<>();
        new ListVisits().service(repository, new VisitMapperImpl(), response);

        assertThat(response.getObject().getStatusCode().value()).isEqualTo(200);
        assertThat(response.getObject().getBody()).hasSize(1);
        assertThat(response.getObject().getBody().get(0).getDescription()).isEqualTo("rabies shot");
    }

    @Test
    void respondsNotFoundWhenEmpty() {
        VisitRepository repository = mock(VisitRepository.class);
        when(repository.findAll()).thenReturn(List.of());

        MockObjectResponse<ResponseEntity<List<VisitDto>>> response = new MockObjectResponse<>();
        new ListVisits().service(repository, new VisitMapperImpl(), response);

        assertThat(response.getObject().getStatusCode().value()).isEqualTo(404);
    }
}
