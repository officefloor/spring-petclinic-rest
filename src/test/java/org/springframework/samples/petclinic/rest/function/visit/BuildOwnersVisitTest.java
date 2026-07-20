package org.springframework.samples.petclinic.rest.function.visit;

import java.time.LocalDate;

import net.officefloor.model.test.variable.MockVar;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.mapper.VisitMapperImpl;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.dto.VisitFieldsDto;

import static org.assertj.core.api.Assertions.assertThat;

class BuildOwnersVisitTest {

    @Test
    void buildsVisitAssociatedWithPetFromThePath() {
        VisitFieldsDto request = new VisitFieldsDto().description("rabies shot").date(LocalDate.of(2020, 1, 15));

        MockVar<Visit> built = new MockVar<>();
        new BuildOwnersVisit().service(request, 3, new VisitMapperImpl(), built);

        Visit visit = built.get();
        assertThat(visit.getDescription()).isEqualTo("rabies shot");
        assertThat(visit.getDate()).isEqualTo(LocalDate.of(2020, 1, 15));
        assertThat(visit.getPet().getId()).isEqualTo(3);
    }
}
