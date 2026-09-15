package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Rejects a create request whose last name and address already belong to an existing
 * owner, comparing both case-insensitively with collapsed whitespace. Runs before
 * {@link BuildOwner}, so a collision is a 409 via {@link DuplicateHouseholdException}
 * before any owner is built or saved. A request that opts in with
 * {@code sharesHousehold=true} is allowed through; {@link AssignHousehold} then groups it
 * with the existing owners under a shared household id.
 */
public class EnsureUniqueHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        if (!Households.membersAt(ownerRepository, request.getLastName(), request.getAddress()).isEmpty()) {
            throw new DuplicateHouseholdException(request.getLastName(), request.getAddress());
        }
    }
}
