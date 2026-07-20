package org.springframework.samples.petclinic.rest.function.visit;

import net.officefloor.model.test.variable.MockVar;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.rest.dto.VisitFieldsDto;

import static org.assertj.core.api.Assertions.assertThat;

class ValidateVisitFieldsTest {

    @Test
    void publishesTheValidatedRequest() {
        VisitFieldsDto request = new VisitFieldsDto().description("rabies shot");

        MockVar<VisitFieldsDto> validated = new MockVar<>();
        new ValidateVisitFields().service(request, validated);

        assertThat(validated.get()).isSameAs(request);
    }
}
