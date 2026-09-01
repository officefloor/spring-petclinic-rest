package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Rejects a new owner whose deterministic {@code householdId} (derived from last name and postcode by
 * {@link AssignHousehold}) already belongs to another owner. Skipped when the request opts in with
 * {@code sharesHousehold: true}, in which case the owner is created as a declared household member.
 * Runs after {@link AssignHousehold} and before Save, so the conflict is a 409, not a persisted
 * duplicate.
 */
public class EnsureUniqueHousehold {

    public void service(@Val Owner owner, @Val Boolean sharesHousehold, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(sharesHousehold)) {
            return;
        }
        String householdId = owner.getHouseholdId();
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue;
            }
            if (householdId.equals(existing.getHouseholdId())) {
                throw new DuplicateHouseholdException(owner.getLastName(), owner.getAddress());
            }
        }
    }
}
