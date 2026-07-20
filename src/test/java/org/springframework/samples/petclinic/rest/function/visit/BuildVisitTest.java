package org.springframework.samples.petclinic.rest.function.visit;

import java.time.LocalDate;

import net.officefloor.model.test.variable.MockVar;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.mapper.VisitMapperImpl;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.dto.VisitDto;

import static org.assertj.core.api.Assertions.assertThat;

class BuildVisitTest {

    @Test
    void mapsRequestToNewVisitWithPetIdFromBody() {
        VisitDto request = new VisitDto().description("rabies shot").date(LocalDate.of(2020, 1, 15)).petId(3);

        MockVar<Visit> built = new MockVar<>();
        new BuildVisit().service(request, new VisitMapperImpl(), built);

        Visit visit = built.get();
        assertThat(visit.getDescription()).isEqualTo("rabies shot");
        assertThat(visit.getDate()).isEqualTo(LocalDate.of(2020, 1, 15));
        assertThat(visit.getPet().getId()).isEqualTo(3);
    }
}
