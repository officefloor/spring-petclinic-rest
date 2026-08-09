package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerEmailConflictException;

/**
 * Rejects a create-owner request whose lower-cased email is already used by any other owner.
 * The email was normalized (trimmed and lower-cased) by {@link ValidateOwnerFields} and published on
 * the body; this step compares that value against the lower-cased email of every existing owner and
 * throws {@link OwnerEmailConflictException} (handled as 409) on a match. An absent email never
 * conflicts.
 */
public class CheckOwnerEmailUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerEmailConflictException {
        String email = request.getEmail();
        if (email == null) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (email.equals(normalize(existing.getEmail()))) {
                throw new OwnerEmailConflictException(request.getEmail());
            }
        }
    }

    private static String normalize(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
