package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

import net.officefloor.plugin.variable.Val;

/**
 * Step of {@code POST /api/owners} that runs after {@link ValidateOwnerFields} has lower-cased the
 * email. Rejects the request when any existing owner already uses that same lower-cased email, so a
 * duplicate is a 409 Conflict rather than two owners sharing an email.
 *
 * <p>Email is optional; when the request has no email there is nothing to conflict with and the
 * check is skipped.
 */
public class RejectDuplicateEmail {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = request.getEmail();
        if (email == null) {
            return;
        }
        String normalized = email.toLowerCase(Locale.ROOT);
        for (Owner existing : ownerRepository.findAll()) {
            String existingEmail = existing.getEmail();
            if (existingEmail != null && normalized.equals(existingEmail.toLowerCase(Locale.ROOT))) {
                throw new DuplicateEmailException(normalized);
            }
        }
    }
}
