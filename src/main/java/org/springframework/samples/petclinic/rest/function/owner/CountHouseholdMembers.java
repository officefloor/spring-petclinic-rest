package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Computes the owner's {@code householdSize} — the number of members this owner's household (owners
 * sharing the same {@code householdId}) has after this create: the owners that already exist with the
 * same {@code householdId} plus this new owner. Assigned at creation and then persisted and returned,
 * so it fixes the {@code membershipTier} ('GOLD' at 3 or more) at the moment of creation.
 *
 * <p>Runs after {@link AssignHousehold} (which sets {@code householdId}) and before {@link SaveOwner}
 * so the new owner is not yet persisted and is counted once via the {@code + 1}.
 */
public class CountHouseholdMembers {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        int count = 1;
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId != null && householdId.equals(existing.getHouseholdId())) {
                count++;
            }
        }
        owner.setHouseholdSize(count);
    }
}
