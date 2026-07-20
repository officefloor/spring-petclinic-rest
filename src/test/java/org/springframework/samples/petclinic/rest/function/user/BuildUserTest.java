package org.springframework.samples.petclinic.rest.function.user;

import net.officefloor.model.test.variable.MockVar;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.mapper.UserMapperImpl;
import org.springframework.samples.petclinic.model.User;
import org.springframework.samples.petclinic.rest.dto.RoleDto;
import org.springframework.samples.petclinic.rest.dto.UserDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BuildUserTest {

    @Test
    void mapsRequestToNewUser() {
        UserDto request = new UserDto().username("username").password("password").enabled(true)
            .roles(List.of(new RoleDto().name("OWNER_ADMIN")));

        MockVar<User> built = new MockVar<>();
        new BuildUser().service(request, new UserMapperImpl(), built);

        User user = built.get();
        assertThat(user.getUsername()).isEqualTo("username");
        assertThat(user.getPassword()).isEqualTo("password");
        assertThat(user.getRoles()).hasSize(1);
    }
}
