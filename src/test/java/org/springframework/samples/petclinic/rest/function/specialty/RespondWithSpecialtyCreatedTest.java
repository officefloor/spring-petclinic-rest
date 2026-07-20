package org.springframework.samples.petclinic.rest.function.specialty;

import net.officefloor.woof.mock.MockObjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.SpecialtyMapperImpl;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.rest.dto.SpecialtyDto;

import static org.assertj.core.api.Assertions.assertThat;

class RespondWithSpecialtyCreatedTest {

    @Test
    void respondsCreatedWithLocationAndBody() {
        Specialty specialty = new Specialty();
        specialty.setId(9);
        specialty.setName("radiology");

        MockObjectResponse<ResponseEntity<SpecialtyDto>> response = new MockObjectResponse<>();
        new RespondWithSpecialtyCreated().service(specialty, new SpecialtyMapperImpl(), response);

        ResponseEntity<SpecialtyDto> result = response.getObject();
        assertThat(result.getStatusCode().value()).isEqualTo(201);
        assertThat(result.getHeaders().getLocation().toString()).isEqualTo("/api/specialties/9");
        assertThat(result.getBody().getName()).isEqualTo("radiology");
    }
}
