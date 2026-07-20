package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.woof.mock.MockObjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RespondWithOwnerUpdatedTest {

    @Test
    void sendsNoContentStatus() {
        Owner owner = new Owner();
        OwnerMapper mapper = mock(OwnerMapper.class);
        when(mapper.toOwnerDto(owner)).thenReturn(new OwnerDto());

        MockObjectResponse<ResponseEntity<OwnerDto>> response = new MockObjectResponse<>();
        new RespondWithOwnerUpdated().service(owner, mapper, response);

        assertThat(response.getObject().getStatusCode().value()).isEqualTo(204);
    }
}
