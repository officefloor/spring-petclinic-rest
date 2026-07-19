package org.springframework.samples.petclinic.rest.function.visit;

import net.officefloor.woof.mock.MockObjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.VisitMapperImpl;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.dto.VisitDto;

import static org.assertj.core.api.Assertions.assertThat;

class RespondWithVisitCreatedTest {

    @Test
    void respondsCreatedWithLocationAndBody() {
        Visit visit = new Visit();
        visit.setId(9);
        visit.setDescription("rabies shot");

        MockObjectResponse<ResponseEntity<VisitDto>> response = new MockObjectResponse<>();
        new RespondWithVisitCreated().service(visit, new VisitMapperImpl(), response);

        ResponseEntity<VisitDto> result = response.getObject();
        assertThat(result.getStatusCode().value()).isEqualTo(201);
        assertThat(result.getHeaders().getLocation().toString()).isEqualTo("/api/visits/9");
        assertThat(result.getBody().getDescription()).isEqualTo("rabies shot");
    }
}
