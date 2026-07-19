package org.springframework.samples.petclinic.rest.function.specialty;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.rest.dto.SpecialtyDto;

import static org.assertj.core.api.Assertions.assertThat;

class ApplySpecialtyTest {

    @Test
    void copiesNameOntoTheEntity() {
        Specialty specialty = new Specialty();
        specialty.setId(2);
        specialty.setName("old");

        SpecialtyDto request = new SpecialtyDto().name("radiology");

        new ApplySpecialty().service(specialty, request);

        assertThat(specialty.getName()).isEqualTo("radiology");
        assertThat(specialty.getId()).isEqualTo(2);
    }
}
