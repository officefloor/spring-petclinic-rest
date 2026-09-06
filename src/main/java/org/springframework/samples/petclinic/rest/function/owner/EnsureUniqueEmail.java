package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;

/**
 * Rejects creating an owner whose lower-cased email is already used by another owner.
 * Email is optional, so a missing/blank email skips the check; when present it has
 * already been normalized to lower case by {@link BuildOwner}.
 */
public class EnsureUniqueEmail {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = owner.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        boolean inUse = ownerRepository.findByEmail(email).stream()
                .anyMatch(existing -> !existing.getId().equals(owner.getId()));
        if (inUse) {
            throw new DuplicateEmailException(
                    "An owner with email " + email + " already exists");
        }
    }
}
