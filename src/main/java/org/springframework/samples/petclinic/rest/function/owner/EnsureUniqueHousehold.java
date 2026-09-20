package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create request that would place a new owner in a household already occupied by
 * an existing owner — one sharing the same last name and address, compared case-insensitively
 * with collapsed whitespace (see {@link Households#normalize(String)}). A request may opt in
 * to a shared household by setting {@code sharesHousehold} true, in which case the check is
 * skipped.
 */
public class EnsureUniqueHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = Households.normalize(request.getLastName());
        String address = Households.normalize(request.getAddress());
        for (Owner owner : ownerRepository.findAll()) {
            if (lastName.equals(Households.normalize(owner.getLastName()))
                    && address.equals(Households.normalize(owner.getAddress()))) {
                throw new DuplicateHouseholdException(request.getLastName(), request.getAddress());
            }
        }
    }
}
