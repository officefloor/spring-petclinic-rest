package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;

/**
 * Step of {@code POST /api/owners} that rejects a create whose email is already used by an existing
 * owner, throwing {@link DuplicateEmailException} (handled as 409 Conflict). Runs after
 * {@link RequireOwnerFields} has normalized the email to its trimmed, lower-cased form and published
 * the body, and before {@link BuildOwner}/{@link SaveOwner} persist the new owner. Email is optional:
 * a request without an email is never a duplicate. Duplicates are detected by comparing lower-cased
 * values.
 */
public class RequireUniqueEmail {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = request.getEmail(); // already trimmed and lower-cased by RequireOwnerFields
        if (email == null || email.isBlank()) {
            return; // email is optional; absent email is never a duplicate
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (email.equals(normalize(existing.getEmail()))) {
                throw new DuplicateEmailException(
                        "Email " + email + " is already used by another owner");
            }
        }
    }

    private static String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
