package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Rejects a create-owner request that shares both last name and address with an existing owner.
 * Last name and address are each compared case-insensitively with runs of whitespace collapsed to a
 * single space (and leading/trailing whitespace trimmed). Throws {@link DuplicateHouseholdException}
 * (handled as 409) on a collision, unless the request opted in with {@code sharesHousehold: true}.
 */
public class CheckOwnerHouseholdUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        // The request may opt out of the household-uniqueness rule.
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (OwnerHousehold.sameHousehold(request.getLastName(), request.getAddress(),
                    existing.getLastName(), existing.getAddress())) {
                throw new DuplicateHouseholdException(request.getLastName(), request.getAddress());
            }
        }
    }
}
