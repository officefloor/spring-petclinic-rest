package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code householdSize}: the number of members in the owner's household
 * (owners sharing the same {@code householdId}) after this create — the already-stored members
 * plus this new owner. Runs after {@link AssignHousehold} (so the new owner's {@code householdId}
 * is set and existing members are backfilled) and before {@code save} (so the new owner is not yet
 * counted among the stored members, and is instead added as the {@code + 1}).
 *
 * <p>An owner not part of a shared household ({@code householdId} null) has a household size of 1.
 */
public class AssignHouseholdSize {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        int count = 1; // this new owner, not yet saved
        if (householdId != null) {
            for (Owner existing : ownerRepository.findAll()) {
                if (householdId.equals(existing.getHouseholdId())) {
                    count++;
                }
            }
        }
        owner.setHouseholdSize(count);
    }
}
