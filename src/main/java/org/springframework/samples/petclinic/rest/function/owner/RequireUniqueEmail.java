package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;

/**
 * Runs after {@link NormalizeOwnerEmail} in the create-owner pipeline: rejects the request with a
 * 409 (via {@code DuplicateEmailExceptionHandler}) when the lower-cased email is already used by any
 * other owner. Email is optional, so a blank (absent) value is left alone. Both sides are normalized
 * through the shared {@link EmailNormalizer} rule so an owner stored with a differently-cased address
 * still counts as a match. The new owner is not persisted yet, so every match found is another owner.
 */
public class RequireUniqueEmail {

    public void service(@Val OwnerFieldsDto owner, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = owner.getEmail();
        if (EmailNormalizer.isBlank(email)) {
            return;
        }
        String normalized = EmailNormalizer.normalize(email);
        for (Owner existing : ownerRepository.findAll()) {
            String other = existing.getEmail();
            if (!EmailNormalizer.isBlank(other) && normalized.equals(EmailNormalizer.normalize(other))) {
                throw new DuplicateEmailException(normalized);
            }
        }
    }
}
