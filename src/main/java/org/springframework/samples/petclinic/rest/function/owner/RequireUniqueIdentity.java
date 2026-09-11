package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Rejects a create request that would join an existing household without declaring it.
 * The household is keyed on (lastName, postcode) via {@link Households}, so an incoming
 * owner whose derived {@code householdId} already belongs to an existing owner is a
 * household duplicate, reported as a 409 by
 * {@link org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityExceptionHandler}.
 *
 * <p>Setting {@code sharesHousehold} bypasses this block: the owner is then created as a
 * declared household member (and, being declared, is not flagged as a possible duplicate by
 * {@link AssignPossibleDuplicate}). Owners in different households always derive a different
 * {@code householdId}, so they never collide here.
 */
public class RequireUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // a declared household member bypasses the duplicate block
        }
        String householdId = Households.idFor(request.getLastName(), request.getPostcode());
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner does not block a new create
            }
            if (householdId.equals(existing.getHouseholdId())) {
                throw new DuplicateIdentityException(householdId);
            }
        }
    }
}
