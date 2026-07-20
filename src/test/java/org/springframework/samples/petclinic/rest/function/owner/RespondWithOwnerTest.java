package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.woof.mock.MockObjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RespondWithOwnerTest {

    @Test
    void mapsEntityToDto() {
        Owner owner = new Owner();
        OwnerDto dto = new OwnerDto();
        OwnerMapper mapper = mock(OwnerMapper.class);
        when(mapper.toOwnerDto(owner)).thenReturn(dto);

        MockObjectResponse<OwnerDto> response = new MockObjectResponse<>();
        new RespondWithOwner().service(owner, mapper, response);

        assertThat(response.getObject()).isSameAs(dto);
    }
}
