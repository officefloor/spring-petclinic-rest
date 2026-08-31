package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;

/**
 * On create, rejects the owner when its lower-cased email already belongs to another
 * owner. Owners without an email are ignored, so a blank email never collides.
 */
public class EnsureUniqueOwnerEmail {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = normalize(owner.getEmail());
        if (email.isEmpty()) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (!Objects.equals(existing.getId(), owner.getId())
                    && email.equals(normalize(existing.getEmail()))) {
                throw new DuplicateEmailException("Email is already used by another owner");
            }
        }
    }

    private static String normalize(String email) {
        return email == null ? "" : email.toLowerCase();
    }
}
