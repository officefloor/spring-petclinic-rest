package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Rejects a create request whose household (the {@code householdId} computed from last name and
 * postcode) matches an existing owner, so a second owner in the same household is a 409 via
 * {@link DuplicateHouseholdException}. A request that opts in with {@code sharesHousehold} true is
 * allowed through as a declared household member.
 */
public class EnsureUniqueHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String householdId = HouseholdId.of(request.getLastName(), request.getPostcode());
        for (Owner owner : ownerRepository.findAll()) {
            if (householdId.equals(owner.getHouseholdId())) {
                throw new DuplicateHouseholdException(request.getLastName(), request.getAddress());
            }
        }
    }
}
