package org.springframework.samples.petclinic.rest.function.user;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Role;
import org.springframework.samples.petclinic.model.User;
import org.springframework.samples.petclinic.repository.UserRepository;

public class SaveUser {

    public void service(@Val User user, UserRepository userRepository) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            throw new IllegalArgumentException("User must have at least a role set!");
        }
        for (Role role : user.getRoles()) {
            if (!role.getName().startsWith("ROLE_")) {
                role.setName("ROLE_" + role.getName());
            }
            if (role.getUser() == null) {
                role.setUser(user);
            }
        }
        userRepository.save(user);
    }
}
