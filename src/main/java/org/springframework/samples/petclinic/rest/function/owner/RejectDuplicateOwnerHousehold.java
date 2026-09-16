package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Rejects a create-owner request that shares a household — the same last name and address
 * as an existing owner (see {@link OwnerHouseholds}) — responding 409, unless the request
 * opts in with {@code sharesHousehold} true. When it opts in, the request is allowed and
 * {@link AssignOwnerHousehold} links the members with a shared household id.
 */
public class RejectDuplicateOwnerHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        if (!OwnerHouseholds.members(ownerRepository.findAll(),
                request.getLastName(), request.getAddress()).isEmpty()) {
            throw new DuplicateHouseholdException(request.getLastName(), request.getAddress());
        }
    }
}
