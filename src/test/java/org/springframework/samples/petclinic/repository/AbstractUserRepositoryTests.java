package org.springframework.samples.petclinic.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.model.User;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <p> Base class for {@link UserRepository} integration tests. The role-prefixing logic that
 * used to live in UserServiceImpl.saveUser now lives in the {@code SaveUser} REST function, whose
 * behaviour is exercised end-to-end by {@code UserRestControllerV1Tests}. This test exercises
 * persistence of an already-valid object graph only, which is why the back-reference is set
 * explicitly here rather than relied upon. </p>
 */
public abstract class AbstractUserRepositoryTests {

    @Autowired
    protected UserRepository userRepository;

    @Test
    @Transactional
    void shouldSaveUserWithRoles() {
        User user = new User();
        user.setUsername("username");
        user.setPassword("password");
        user.setEnabled(true);
        user.addRole("ROLE_OWNER_ADMIN");
        user.getRoles().forEach(role -> role.setUser(user));

        userRepository.save(user);

        assertThat(user.getRoles()).hasSize(1);
        assertThat(user.getRoles().iterator().next().getName()).isEqualTo("ROLE_OWNER_ADMIN");
        assertThat(user.getRoles().iterator().next().getUser()).isSameAs(user);
    }
}
