package org.springframework.samples.petclinic.rest.function.user;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.model.Role;
import org.springframework.samples.petclinic.model.User;
import org.springframework.samples.petclinic.repository.UserRepository;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SaveUserTest {

    @Test
    void prefixesRoleNamesAndSetsBackReferenceBeforeSaving() {
        User user = new User();
        user.setUsername("username");
        Role role = new Role();
        role.setName("OWNER_ADMIN");
        user.setRoles(Set.of(role));
        UserRepository repository = mock(UserRepository.class);

        new SaveUser().service(user, repository);

        assertThat(role.getName()).isEqualTo("ROLE_OWNER_ADMIN");
        assertThat(role.getUser()).isSameAs(user);
        verify(repository).save(user);
    }

    @Test
    void leavesAlreadyPrefixedRoleNamesUnchanged() {
        User user = new User();
        user.setUsername("username");
        Role role = new Role();
        role.setName("ROLE_OWNER_ADMIN");
        user.setRoles(Set.of(role));
        UserRepository repository = mock(UserRepository.class);

        new SaveUser().service(user, repository);

        assertThat(role.getName()).isEqualTo("ROLE_OWNER_ADMIN");
    }

    @Test
    void doesNotOverwriteAnExistingRoleBackReference() {
        User user = new User();
        user.setUsername("username");
        User otherUser = new User();
        otherUser.setUsername("other");
        Role role = new Role();
        role.setName("ROLE_OWNER_ADMIN");
        role.setUser(otherUser);
        user.setRoles(Set.of(role));
        UserRepository repository = mock(UserRepository.class);

        new SaveUser().service(user, repository);

        assertThat(role.getUser()).isSameAs(otherUser);
    }

    @Test
    void throwsWhenNoRolesSet() {
        User user = new User();
        user.setUsername("username");
        UserRepository repository = mock(UserRepository.class);

        assertThatThrownBy(() -> new SaveUser().service(user, repository))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
