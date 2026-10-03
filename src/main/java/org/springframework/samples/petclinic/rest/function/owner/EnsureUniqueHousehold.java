package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Rejects creating an owner when another owner already has the same last name and the
 * same address, compared case-insensitively with collapsed whitespace (see
 * {@link Households}). The request can opt out of the rule by setting
 * {@code sharesHousehold} true, in which case {@link AssignHousehold} joins the owner to
 * the shared household instead.
 */
public class EnsureUniqueHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = Households.normalize(request.getLastName());
        String address = Households.normalize(request.getAddress());
        for (Owner existing : ownerRepository.findAll()) {
            if (Households.matches(existing, lastName, address)) {
                throw new DuplicateHouseholdException(request.getLastName(), request.getAddress());
            }
        }
    }
}
