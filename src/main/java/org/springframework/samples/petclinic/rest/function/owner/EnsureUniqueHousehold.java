package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * The duplicate block: rejects a create-owner request that would be a second owner in an
 * existing household — same last name and postcode, and therefore the same derived
 * {@code householdId} (see {@link Households}) — responding 409. A request opting in with
 * {@code sharesHousehold} declares the new owner a genuine member of that household and
 * bypasses the block. An owner with no postcode has no household and is never blocked here.
 * Runs after {@link AssignHouseholdId} (so the household id is set) and before
 * {@link SaveOwner} persists a duplicate.
 */
public class EnsureUniqueHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (owner.getHouseholdId() == null || Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        if (!Households.membersOf(ownerRepository, owner.getLastName(), owner.getPostcode()).isEmpty()) {
            throw new DuplicateHouseholdException(owner.getHouseholdId());
        }
    }
}
