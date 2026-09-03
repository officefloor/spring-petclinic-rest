package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Rejects a create when another owner resolves to the same {@link HouseholdId} (same last
 * name and postcode), unless the request opted in with {@code sharesHousehold}.
 */
public class EnsureOwnerHouseholdUnique {

    public void service(@Val Owner owner, @Val Boolean sharesHousehold, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(sharesHousehold)) {
            return;
        }
        String householdId = HouseholdId.of(owner);
        boolean clash = ownerRepository.findAll().stream()
                .filter(other -> !other.isDeleted())
                .anyMatch(other -> householdId.equals(HouseholdId.of(other)));
        if (clash) {
            throw new DuplicateHouseholdException(
                    "An owner with the same last name and postcode already exists");
        }
    }
}
