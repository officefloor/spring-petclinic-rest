package org.springframework.samples.petclinic.rest.function.specialty;

import net.officefloor.model.test.variable.MockVar;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.mapper.SpecialtyMapperImpl;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.rest.dto.SpecialtyDto;

import static org.assertj.core.api.Assertions.assertThat;

class BuildSpecialtyTest {

    @Test
    void mapsRequestToNewSpecialty() {
        SpecialtyDto request = new SpecialtyDto().name("radiology");

        MockVar<Specialty> built = new MockVar<>();
        new BuildSpecialty().service(request, new SpecialtyMapperImpl(), built);

        assertThat(built.get().getName()).isEqualTo("radiology");
    }
}
