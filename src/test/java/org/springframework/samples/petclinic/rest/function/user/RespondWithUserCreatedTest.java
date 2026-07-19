package org.springframework.samples.petclinic.rest.function.user;

import net.officefloor.woof.mock.MockObjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.UserMapperImpl;
import org.springframework.samples.petclinic.model.User;
import org.springframework.samples.petclinic.rest.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

class RespondWithUserCreatedTest {

    @Test
    void respondsCreatedWithBody() {
        User user = new User();
        user.setUsername("username");

        MockObjectResponse<ResponseEntity<UserDto>> response = new MockObjectResponse<>();
        new RespondWithUserCreated().service(user, new UserMapperImpl(), response);

        ResponseEntity<UserDto> result = response.getObject();
        assertThat(result.getStatusCode().value()).isEqualTo(201);
        assertThat(result.getBody().getUsername()).isEqualTo("username");
    }
}
