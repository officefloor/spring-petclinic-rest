package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;

/**
 * Rejects a create-owner request whose lower-cased email is already used by any other
 * owner, with a 409 Conflict. An absent (null/blank) email is not a conflict. Runs after
 * {@link BuildOwner} has normalized the email.
 */
public class CheckOwnerEmailUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = key(owner.getEmail());
        if (email.isEmpty()) {
            return; // no email supplied: nothing to conflict with
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue; // same record (e.g. re-save), not a conflict
            }
            if (email.equals(key(existing.getEmail()))) {
                throw new DuplicateEmailException(owner.getEmail());
            }
        }
    }

    /** Comparison key: lower-cased and trimmed; a null email becomes empty. */
    private static String key(String value) {
        if (value == null) {
            return "";
        }
        return value.strip().toLowerCase(Locale.ROOT);
    }
}
