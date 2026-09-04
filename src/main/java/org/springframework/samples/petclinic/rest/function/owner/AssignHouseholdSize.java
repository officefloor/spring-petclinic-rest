package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records, on the owner being created, the size of its household — the number of owners sharing the
 * same deterministic {@code householdId} once this create completes. The id is keyed on
 * {@code (normalizedLastName, postcode)} (see {@link OwnerIdentityKey}), so this counts existing owners
 * with the same last name and postcode plus one for the owner being created (not yet saved).
 *
 * <p>Runs after {@link AssignHousehold}, which has set the new owner's {@code householdId}, and before
 * {@link SaveOwner}.
 */
public class AssignHouseholdSize {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        int count = 1; // the owner being created, not yet saved
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(OwnerIdentityKey.householdIdOf(existing))) {
                count++;
            }
        }
        owner.setHouseholdSize(count);
    }
}
