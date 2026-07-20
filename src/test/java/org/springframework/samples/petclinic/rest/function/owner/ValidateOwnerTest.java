package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.model.test.variable.MockVar;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

import static org.assertj.core.api.Assertions.assertThat;

class ValidateOwnerTest {

    @Test
    void publishesTheRequest() {
        OwnerFieldsDto request = new OwnerFieldsDto().firstName("George");

        MockVar<OwnerFieldsDto> validated = new MockVar<>();
        new ValidateOwner().service(request, validated);

        assertThat(validated.get()).isSameAs(request);
    }
}
