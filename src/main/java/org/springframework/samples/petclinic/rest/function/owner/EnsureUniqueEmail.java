package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateEmailException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create-owner request whose email is already used by another owner, responding
 * 409. Runs after {@link NormalizeOwnerEmail} so the comparison is between lower-cased
 * values, and before {@link BuildOwner} persists a duplicate. Email is optional: an absent
 * or blank email skips the check.
 */
public class EnsureUniqueEmail {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateEmailException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        if (!ownerRepository.findByEmail(email).isEmpty()) {
            throw new DuplicateEmailException(email);
        }
    }
}
