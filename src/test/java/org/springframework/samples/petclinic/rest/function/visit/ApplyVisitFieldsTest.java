package org.springframework.samples.petclinic.rest.function.visit;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.dto.VisitFieldsDto;

import static org.assertj.core.api.Assertions.assertThat;

class ApplyVisitFieldsTest {

    @Test
    void copiesDateAndDescriptionOntoTheEntityWithoutTouchingPet() {
        Pet pet = new Pet();
        pet.setId(3);
        Visit visit = new Visit();
        visit.setId(2);
        visit.setPet(pet);
        visit.setDescription("old");

        VisitFieldsDto request = new VisitFieldsDto().description("neutered").date(LocalDate.of(2020, 1, 15));

        new ApplyVisitFields().service(visit, request);

        assertThat(visit.getDescription()).isEqualTo("neutered");
        assertThat(visit.getDate()).isEqualTo(LocalDate.of(2020, 1, 15));
        assertThat(visit.getPet()).isSameAs(pet);
    }
}
