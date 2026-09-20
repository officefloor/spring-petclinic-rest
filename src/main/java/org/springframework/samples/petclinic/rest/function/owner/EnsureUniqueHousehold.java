package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create request that would place a new owner in a household already occupied by
 * an existing owner — one sharing the same last name and address, compared in their
 * canonical forms (see {@link Households#matches(org.springframework.samples.petclinic.model.Owner, String, String)}).
 * A request may opt in to a shared household by setting {@code sharesHousehold} true, in
 * which case the check is skipped.
 */
public class EnsureUniqueHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        for (Owner owner : ownerRepository.findAll()) {
            if (Households.matches(owner, request.getLastName(), request.getAddress())) {
                throw new DuplicateHouseholdException(request.getLastName(), request.getAddress());
            }
        }
    }
}
