package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerEmailException;

/**
 * Rejects 409 when the built owner's email is already used by any other owner. Runs after
 * {@link NormalizeOwnerEmail}, so this owner's email is already lower-cased (and email is optional,
 * so a null/blank value skips the check); existing owners' emails are lower-cased the same way
 * before comparing.
 */
public class RejectDuplicateOwnerEmail {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateOwnerEmailException {
        String email = owner.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        for (Owner other : ownerRepository.findAll()) {
            if (other != owner && email.equals(lower(other.getEmail()))) {
                throw new DuplicateOwnerEmailException(email);
            }
        }
    }

    private static String lower(String email) {
        return email == null ? null : email.toLowerCase(Locale.ROOT);
    }
}
