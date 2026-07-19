package org.springframework.samples.petclinic.rest.function.pettype;

import net.officefloor.model.test.variable.MockVar;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.rest.dto.PetTypeDto;

import static org.assertj.core.api.Assertions.assertThat;

class ValidatePetTypeTest {

    @Test
    void publishesTheValidatedRequest() {
        PetTypeDto request = new PetTypeDto().name("dog");

        MockVar<PetTypeDto> validated = new MockVar<>();
        new ValidatePetType().service(request, validated);

        assertThat(validated.get()).isSameAs(request);
    }
}
