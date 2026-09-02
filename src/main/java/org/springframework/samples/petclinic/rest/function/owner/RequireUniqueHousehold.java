package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerHouseholdException;

/**
 * Rejects a create request that shares another owner's household, i.e. has the same computed
 * {@code householdId} (last name and postcode, see {@link HouseholdId}). A request with
 * {@code sharesHousehold} true opts out and is created as a declared household member. Requests
 * without a postcode are not grouped into a household. A match rejects 409.
 */
public class RequireUniqueHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())
                || request.getPostcode() == null || request.getPostcode().isBlank()) {
            return;
        }
        String household = HouseholdId.of(request.getLastName(), request.getPostcode());
        for (Owner owner : ownerRepository.findAll()) {
            if (household.equals(HouseholdId.of(owner.getLastName(), owner.getPostcode()))) {
                throw new DuplicateOwnerHouseholdException(owner.getLastName(), owner.getAddress());
            }
        }
    }
}
