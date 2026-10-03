package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;

/**
 * Rejects creating an owner whose lower-cased email is already used by any other owner.
 * Runs after {@link NormalizeOwnerEmail} (so the owner's email is already lower-cased) and
 * before {@link SaveOwner}. Email is optional: when the owner has no email the check is
 * skipped. Stored emails are compared lower-cased so a stored value in any case still
 * collides. A match on a different owner escalates as a 409 via {@link DuplicateEmailException}.
 */
public class RejectDuplicateEmail {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = owner.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue;
            }
            String existingEmail = existing.getEmail();
            if (existingEmail != null && email.equals(existingEmail.toLowerCase())) {
                throw new DuplicateEmailException(
                        "Email is already used by another owner");
            }
        }
    }
}
