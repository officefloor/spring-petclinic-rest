package org.springframework.samples.petclinic.rest.function.vet;

import net.officefloor.model.test.variable.MockVar;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.rest.dto.VetDto;

import static org.assertj.core.api.Assertions.assertThat;

class ValidateVetTest {

    @Test
    void publishesTheValidatedRequest() {
        VetDto request = new VetDto().firstName("James").lastName("Carter");

        MockVar<VetDto> validated = new MockVar<>();
        new ValidateVet().service(request, validated);

        assertThat(validated.get()).isSameAs(request);
    }
}
