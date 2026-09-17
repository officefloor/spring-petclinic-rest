package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Rejects a create request that would place the new owner in a household already occupied by an
 * existing owner — same last name and address, compared case-insensitively with whitespace collapsed
 * (see {@link HouseholdNormalizer}). A collision is a 409 via {@link DuplicateHouseholdException},
 * unless the request sets {@code sharesHousehold} true to acknowledge the shared household.
 */
public class EnsureUniqueHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String key = HouseholdNormalizer.key(request.getLastName(), request.getAddress());
        for (Owner owner : ownerRepository.findAll()) {
            if (key.equals(HouseholdNormalizer.key(owner.getLastName(), owner.getAddress()))) {
                throw new DuplicateHouseholdException(request.getLastName(), request.getAddress());
            }
        }
    }
}
