package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records, on the owner being created, the size of its household — the number of owners sharing the
 * same {@code householdId} once this create completes. Runs after {@link AssignHousehold}, which has
 * already assigned the shared {@code householdId} to the new owner and any existing members, but
 * before {@link SaveOwner} persists the new owner; so the count is the existing members carrying the
 * id plus one for the owner being created.
 *
 * <p>An owner with no {@code householdId} (no shared household) has a household size of one.
 */
public class AssignHouseholdSize {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            owner.setHouseholdSize(1);
            return;
        }
        int count = 1; // the owner being created, not yet saved
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(existing.getHouseholdId())) {
                count++;
            }
        }
        owner.setHouseholdSize(count);
    }
}
