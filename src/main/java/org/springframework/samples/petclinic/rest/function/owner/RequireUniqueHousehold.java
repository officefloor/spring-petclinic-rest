package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Rejects a create-owner request that shares a household with an existing owner — same last
 * name and address once normalized (see {@link OwnerHouseholds}) — responding 409 via
 * {@link DuplicateHouseholdException}. A request that opts in with {@code sharesHousehold}
 * true is allowed through.
 */
public class RequireUniqueHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String key = OwnerHouseholds.key(request.getLastName(), request.getAddress());
        for (Owner existing : ownerRepository.findAll()) {
            if (key.equals(OwnerHouseholds.key(existing.getLastName(), existing.getAddress()))) {
                throw new DuplicateHouseholdException(request.getLastName(), request.getAddress());
            }
        }
    }
}
