package org.springframework.samples.petclinic.rest.function.pet;

import net.officefloor.woof.mock.MockObjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.rest.dto.PetDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RespondWithPetTest {

    @Test
    void mapsEntityToDto() {
        Pet pet = new Pet();
        PetDto dto = new PetDto();
        PetMapper mapper = mock(PetMapper.class);
        when(mapper.toPetDto(pet)).thenReturn(dto);

        MockObjectResponse<PetDto> response = new MockObjectResponse<>();
        new RespondWithPet().service(pet, mapper, response);

        assertThat(response.getObject()).isSameAs(dto);
    }
}
