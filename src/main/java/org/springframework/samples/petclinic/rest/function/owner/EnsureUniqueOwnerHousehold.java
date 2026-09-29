package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Rejects a create whose lastName and address already belong to an existing owner
 * (see {@link OwnerHousehold} for the membership rule). A clash is rejected 409 via
 * {@link DuplicateHouseholdException}, unless the request opts in with
 * {@code sharesHousehold} true — in which case {@link AssignOwnerHousehold} gives the
 * housemates a shared id.
 */
public class EnsureUniqueOwnerHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        // Opt-in flag lets known housemates share a lastName and address.
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (OwnerHousehold.isSameHousehold(request.getLastName(), request.getAddress(), existing)) {
                throw new DuplicateHouseholdException(request.getLastName(), request.getAddress());
            }
        }
    }
}
