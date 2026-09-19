package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create-owner request that would share a household (same last name and same
 * address) with an existing owner, responding 409. Last name and address are compared
 * case-insensitively with collapsed whitespace (see {@link Households}). A request may
 * opt in to sharing a household by setting {@code sharesHousehold} true, in which case
 * the check is skipped and {@link AssignHouseholdId} links the owners instead.
 */
public class EnsureUniqueHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        if (!Households.membersOf(ownerRepository, request.getLastName(), request.getAddress()).isEmpty()) {
            throw new DuplicateHouseholdException(request.getLastName(), request.getAddress());
        }
    }
}
