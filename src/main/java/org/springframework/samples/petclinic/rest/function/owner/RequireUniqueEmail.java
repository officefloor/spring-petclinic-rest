package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;

/**
 * Step of {@code POST /api/owners} that runs after {@link RequireOwnerFields} has normalized
 * the email to its lower-cased form. Rejects the create with 409 when that email is already
 * used by any existing owner, comparing each stored email by its lower-cased value so
 * differing letter cases for the same address still count as duplicates. A request without
 * an email is left untouched.
 */
public class RequireUniqueEmail {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = normalize(request.getEmail());
        if (email == null) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (email.equals(normalize(existing.getEmail()))) {
                throw new DuplicateEmailException(request.getEmail());
            }
        }
    }

    private static String normalize(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase();
    }
}
