package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Rejects a create request whose computed {@code householdId} (last name and postcode) already
 * belongs to another owner: they are the same household. A request that opts in with
 * {@code sharesHousehold=true} is allowed through as a declared household member. A request with no
 * postcode has no household and is left untouched. Responds 409 on a clash.
 */
public class RejectDuplicateHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String householdId = IdentityKey.household(request.getLastName(), request.getPostcode());
        if (householdId == null) {
            return;
        }
        for (Owner owner : ownerRepository.findAll()) {
            if (!owner.isDeleted() && householdId.equals(owner.getHouseholdId())) {
                throw new DuplicateHouseholdException(request.getLastName(), request.getPostcode());
            }
        }
    }
}
