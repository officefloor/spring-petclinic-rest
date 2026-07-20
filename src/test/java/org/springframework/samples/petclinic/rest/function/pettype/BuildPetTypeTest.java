package org.springframework.samples.petclinic.rest.function.pettype;

import net.officefloor.model.test.variable.MockVar;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.mapper.PetTypeMapperImpl;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.rest.dto.PetTypeFieldsDto;

import static org.assertj.core.api.Assertions.assertThat;

class BuildPetTypeTest {

    @Test
    void mapsRequestToNewPetTypeWithNoId() {
        PetTypeFieldsDto request = new PetTypeFieldsDto().name("dog");

        MockVar<PetType> built = new MockVar<>();
        new BuildPetType().service(request, new PetTypeMapperImpl(), built);

        assertThat(built.get().getId()).isNull();
        assertThat(built.get().getName()).isEqualTo("dog");
    }
}
