package org.springframework.samples.petclinic.rest.function.specialty;

import net.officefloor.woof.mock.MockObjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.mapper.SpecialtyMapperImpl;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.rest.dto.SpecialtyDto;

import static org.assertj.core.api.Assertions.assertThat;

class RespondWithSpecialtyTest {

    @Test
    void mapsEntityToDto() {
        Specialty specialty = new Specialty();
        specialty.setId(2);
        specialty.setName("radiology");

        MockObjectResponse<SpecialtyDto> response = new MockObjectResponse<>();
        new RespondWithSpecialty().service(specialty, new SpecialtyMapperImpl(), response);

        assertThat(response.getObject().getId()).isEqualTo(2);
        assertThat(response.getObject().getName()).isEqualTo("radiology");
    }
}
