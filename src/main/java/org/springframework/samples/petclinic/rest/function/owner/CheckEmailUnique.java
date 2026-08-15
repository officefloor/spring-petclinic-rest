package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;

/**
 * Rejects a create-owner request whose email is already used by another owner, so the escalation
 * handler can respond 409. Runs after {@link ValidateOwnerFields} has lower-cased the email and
 * before {@link BuildOwner}; duplicates are detected by comparing lower-cased values, so emails that
 * differ only in case collide. Email is optional, so a missing/blank email is accepted.
 */
public class CheckEmailUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
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
