package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerIdentityException;

/**
 * The duplicate-detection step of {@code POST /api/owners}. Because a household is now keyed on
 * (last name, postcode), two owners that share a last name and postcode are the same household (see
 * {@link Household}); creating a second owner in an existing household is therefore treated as a
 * duplicate and rejected with 409.
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
        String householdId = Household.idFor(request);
        for (Owner owner : ownerRepository.findAll()) {
            if (owner.isDeleted()) {
                continue; // a soft-deleted owner no longer blocks a new create
            }
            if (householdId.equals(Household.idFor(owner))) {
                throw new DuplicateOwnerIdentityException(householdId);
            }
        }
    }
}
