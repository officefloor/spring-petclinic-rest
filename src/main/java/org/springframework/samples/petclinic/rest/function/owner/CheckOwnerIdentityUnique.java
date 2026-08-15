package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerIdentityException;

/**
 * The duplicate-detection step of {@code POST /api/owners}. Duplicate detection is expressed through
 * the owner's {@code identityKey} (see {@link OwnerIdentityKey}): a request is a duplicate only when
 * an existing owner carries the <em>same whole key</em> — normalized telephone, email and
 * {@code householdId} all equal. Because the telephone is part of the key, two members of the same
 * household (same last name and postcode) with different telephones have different keys and are both
 * allowed; only a genuine re-submission of the same owner is rejected with 409.
 *
 * <p>The {@code sharesHousehold} hint bypasses this block: a request that declares itself a member
 * of a shared household is allowed through and created as a legitimate second member (and is not
 * flagged as a possible duplicate, see {@link AssignPossibleDuplicate}).
 *
 * <p>Runs after {@link ValidateNewOwner} has normalized the request and before {@link BuildOwner},
 * so a duplicate is a 409 rather than a persisted record.
 */
public class CheckOwnerIdentityUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerIdentityException {
        if (Household.sharesHousehold(request)) {
            return; // declared household member: bypass the duplicate block
        }
        String identityKey = OwnerIdentityKey.forRequest(request);
        for (Owner owner : ownerRepository.findAll()) {
            if (owner.isDeleted()) {
                continue; // a soft-deleted owner no longer blocks a new create
            }
            if (identityKey.equals(OwnerIdentityKey.forOwner(owner))) {
                throw new DuplicateOwnerIdentityException(Household.idFor(request));
            }
        }
    }
}
