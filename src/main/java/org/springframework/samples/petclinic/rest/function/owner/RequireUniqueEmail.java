package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;

/**
 * Step of {@code POST /api/owners}: rejects the request 409 when its email matches, compared as a
 * lower-cased value, the email of any existing owner. Email is optional, so a request with no email
 * is left untouched. Runs after {@link NormalizeOwnerEmail} (which lower-cases and cleans the
 * request email) and before {@link BuildOwner} so no owner is persisted on conflict.
 */
public class RequireUniqueEmail {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            String existingEmail = existing.getEmail();
            if (existingEmail != null
                    && email.equals(existingEmail.trim().toLowerCase(Locale.ROOT))) {
                throw new DuplicateEmailException(email);
            }
        }
    }
}
