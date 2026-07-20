package org.springframework.samples.petclinic.rest.function.specialty;

import net.officefloor.model.test.variable.MockVar;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.rest.dto.SpecialtyDto;

import static org.assertj.core.api.Assertions.assertThat;

class ValidateSpecialtyTest {

    @Test
    void publishesTheValidatedRequest() {
        SpecialtyDto request = new SpecialtyDto().name("radiology");

        MockVar<SpecialtyDto> validated = new MockVar<>();
        new ValidateSpecialty().service(request, validated);

        assertThat(validated.get()).isSameAs(request);
    }
}
