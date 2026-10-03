package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code householdSize}: the number of members the owner's
 * household (owners sharing the same {@code householdId}) has once this owner is
 * created. Runs after {@link AssignHousehold} has set the new owner's
 * {@code householdId} and back-filled any existing members, so the count sees the whole
 * household: it counts the existing owners that carry the same {@code householdId} and
 * adds one for the owner being created (not yet saved). A solo owner (no
 * {@code householdId}) has a household size of one.
 *
 * <p>Drives {@link Owner#getMembershipTier()}, which is {@code "GOLD"} once the
 * household has three or more members.
 */
public class CountHousehold {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            owner.setHouseholdSize(1);
            return;
        }
        int count = 1; // the owner being created, not yet persisted
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(existing.getHouseholdId())) {
                count++;
            }
        }
        owner.setHouseholdSize(count);
    }
}
