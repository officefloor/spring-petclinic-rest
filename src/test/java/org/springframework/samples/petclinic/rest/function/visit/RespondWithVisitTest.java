package org.springframework.samples.petclinic.rest.function.visit;

import java.time.LocalDate;

import net.officefloor.woof.mock.MockObjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.mapper.VisitMapperImpl;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.dto.VisitDto;

import static org.assertj.core.api.Assertions.assertThat;

class RespondWithVisitTest {

    @Test
    void mapsEntityToDto() {
        Visit visit = new Visit();
        visit.setId(2);
        visit.setDate(LocalDate.now());
        visit.setDescription("rabies shot");

        MockObjectResponse<VisitDto> response = new MockObjectResponse<>();
        new RespondWithVisit().service(visit, new VisitMapperImpl(), response);

        assertThat(response.getObject().getId()).isEqualTo(2);
        assertThat(response.getObject().getDescription()).isEqualTo("rabies shot");
    }
}
