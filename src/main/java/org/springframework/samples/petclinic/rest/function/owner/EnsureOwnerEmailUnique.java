package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.OwnerAlreadyExistsException;

/**
 * When an email address is provided, rejects creating an owner whose email is
 * already used by another owner with a 409 conflict. Owners without an email are
 * unaffected. Matching ignores letter case and surrounding or repeated whitespace
 * (see {@link DuplicateKey}).
 */
public class EnsureOwnerEmailUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws OwnerAlreadyExistsException {
        String email = owner.getEmail();
        String emailKey = DuplicateKey.of(email);
        if (emailKey == null) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (Objects.equals(DuplicateKey.of(existing.getEmail()), emailKey)) {
                throw new OwnerAlreadyExistsException(
                        "Owner with email " + email + " already exists");
            }
        }
    }
}
