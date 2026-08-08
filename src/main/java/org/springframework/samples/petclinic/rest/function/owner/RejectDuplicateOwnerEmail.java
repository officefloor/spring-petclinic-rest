package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerEmailException;

/**
 * Rejects a new owner whose lower-cased email is already used by any other owner.
 * Runs after {@link NormalizeOwnerEmail} (so {@code owner} holds the lower-cased
 * email) and before {@link SaveOwner}: if any existing owner has the same
 * lower-cased email, it throws a checked {@link DuplicateOwnerEmailException},
 * which the escalation handler turns into a 409 Conflict.
 */
public class RejectDuplicateOwnerEmail {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateOwnerEmailException {
        String email = lower(owner.getEmail());
        if (email == null) {
            return; // no email; nothing to compare against
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue; // never conflict with the owner itself
            }
            if (email.equals(lower(existing.getEmail()))) {
                throw new DuplicateOwnerEmailException(
                        "Email is already used by another owner");
            }
        }
    }

    private static String lower(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase(Locale.ROOT);
    }
}
