package org.springframework.samples.petclinic.rest.function.pet;

import net.officefloor.model.test.variable.MockVar;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.rest.dto.PetDto;

import static org.assertj.core.api.Assertions.assertThat;

class ValidatePetDtoTest {

    @Test
    void publishesTheValidatedRequest() {
        PetDto request = new PetDto().name("Rosy");

        MockVar<PetDto> validated = new MockVar<>();
        new ValidatePetDto().service(request, validated);

        assertThat(validated.get()).isSameAs(request);
    }
}
