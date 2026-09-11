package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Rejects a create request that exactly duplicates an existing owner's identity. Identity is
 * the {@link IdentityKeys#forFields(String, String, String) identity key} —
 * {@code telephone + email + householdId}, where {@code householdId} is derived from
 * (lastName, postcode) via {@link Households} — so a second owner in the same household is a
 * legitimate, distinct household member (different telephone) rather than a duplicate; only a
 * true resubmit (same telephone, email and household) collides and is reported as a 409 by
 * {@link org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityExceptionHandler}.
 *
 * <p>Setting {@code sharesHousehold} bypasses this block entirely: the owner is then created
 * as a declared household member (and, being declared, is not flagged as a possible duplicate
 * by {@link AssignPossibleDuplicate}). Owners in different households always derive a
 * different {@code householdId}, so they never collide here.
 */
public class RequireUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // a declared household member bypasses the duplicate block
        }
        String householdId = Households.idFor(request.getLastName(), request.getPostcode());
        String identityKey = IdentityKeys.forFields(request.getTelephone(), request.getEmail(),
                householdId);
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner does not block a new create
            }
            if (identityKey.equals(IdentityKeys.forOwner(existing))) {
                throw new DuplicateIdentityException(householdId);
            }
        }
    }
}
