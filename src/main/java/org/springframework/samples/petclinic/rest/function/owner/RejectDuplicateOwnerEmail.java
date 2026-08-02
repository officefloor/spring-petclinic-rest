package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.ConflictException;
import org.springframework.util.StringUtils;

/**
 * Rejects creating an owner whose email address is already used by any other
 * owner, with a 409. Owners without an email are unaffected.
 */
public class RejectDuplicateOwnerEmail {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws ConflictException {
        String email = owner.getEmail();
        if (!StringUtils.hasText(email)) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (!Objects.equals(existing.getId(), owner.getId())
                && Objects.equals(email, existing.getEmail())) {
                throw new ConflictException("An owner with the same email already exists");
            }
        }
    }
}
