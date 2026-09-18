package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Rejects a create-owner request that would add a second owner to a household already occupied by an
 * existing owner — one sharing the same last name and address (see {@link Household}). A request may
 * opt in to sharing a household by setting {@code sharesHousehold} true, which skips the check (the
 * shared {@code householdId} is then assigned by {@link AssignHousehold}). Rejects with 409 otherwise.
 */
public class EnsureUniqueHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        for (Owner existing : ownerRepository.findByLastName(request.getLastName())) {
            if (Household.matches(existing, request.getLastName(), request.getAddress())) {
                throw new DuplicateHouseholdException(request.getLastName(), request.getAddress());
            }
        }
    }
}
