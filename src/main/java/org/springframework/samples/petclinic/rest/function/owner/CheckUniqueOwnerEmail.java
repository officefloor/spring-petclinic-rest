package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Runs after {@link NormalizeOwnerEmail} on {@code POST /api/owners}: rejects the request with 409
 * via {@link DuplicateEmailException} when the normalized (lower-cased) email is already used by any
 * existing owner. Email is optional, so an absent (null or blank) email is never a conflict. Runs
 * before {@link BuildOwner} so no owner is created on conflict.
 */
public class CheckUniqueOwnerEmail {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        String normalized = email.toLowerCase(Locale.ROOT);
        for (Owner existing : ownerRepository.findAll()) {
            String other = existing.getEmail();
            if (other != null && normalized.equals(other.toLowerCase(Locale.ROOT))) {
                throw new DuplicateEmailException(normalized);
            }
        }
    }
}
