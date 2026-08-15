package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Step of {@code POST /api/owners} that consolidates all duplicate detection into a single
 * check on the derived {@link OwnerIdentityKey}. Runs after {@link RequireOwnerFields} has
 * normalized the body. Rejects the create with 409 only when the new owner's WHOLE identityKey
 * (normalizedTelephone + '|' + email + '|' + householdId) equals an existing owner's, so a
 * difference in any single component — including the telephone — makes the two owners distinct
 * and both allowed.
 */
public class RequireUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = OwnerIdentityKey.forRequest(request);
        for (Owner existing : ownerRepository.findAll()) {
            if (identityKey.equals(OwnerIdentityKey.forOwner(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
