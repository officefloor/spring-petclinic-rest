package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Rejects a create-owner request with a 409 (via {@code DuplicateHouseholdExceptionHandler}) when
 * another owner already shares the household — the same last name and the same address, compared
 * case-insensitively after collapsing runs of whitespace. A request that sets {@code sharesHousehold}
 * true opts out of the check and is allowed. The new owner is not persisted yet, so every match found
 * is another owner.
 */
public class RequireUniqueHousehold {

    public void service(@Val OwnerFieldsDto owner, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(owner.getSharesHousehold())) {
            return;
        }
        HouseholdKey household = HouseholdKey.of(owner.getLastName(), owner.getAddress());
        for (Owner existing : ownerRepository.findAll()) {
            if (household.matches(existing)) {
                throw new DuplicateHouseholdException(owner.getLastName(), owner.getAddress());
            }
        }
    }
}
