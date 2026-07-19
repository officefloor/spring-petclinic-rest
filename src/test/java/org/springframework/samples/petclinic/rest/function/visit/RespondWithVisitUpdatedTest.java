package org.springframework.samples.petclinic.rest.function.visit;

import java.time.LocalDate;

import net.officefloor.woof.mock.MockObjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.VisitMapperImpl;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.dto.VisitDto;

import static org.assertj.core.api.Assertions.assertThat;

class RespondWithVisitUpdatedTest {

    @Test
    void respondsNoContent() {
        Visit visit = new Visit();
        visit.setId(2);
        visit.setDate(LocalDate.now());
        visit.setDescription("neutered");

        MockObjectResponse<ResponseEntity<VisitDto>> response = new MockObjectResponse<>();
        new RespondWithVisitUpdated().service(visit, new VisitMapperImpl(), response);

        ResponseEntity<VisitDto> result = response.getObject();
        assertThat(result.getStatusCode().value()).isEqualTo(204);
        assertThat(result.getBody().getDescription()).isEqualTo("neutered");
    }
}
