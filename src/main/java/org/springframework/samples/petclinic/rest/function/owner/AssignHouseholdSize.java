package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners} that records the size of the owner's household after this create:
 * the number of owners sharing the same {@code householdId}, counting the owner being created.
 *
 * <p>Runs after {@link AssignHousehold}, which assigns and back-fills the shared {@code householdId}
 * onto every existing member, and before {@link SaveOwner}. Because the new owner is not yet
 * persisted, it is counted explicitly (the {@code + 1}); existing members are counted from the
 * repository. When the owner has no {@code householdId} (it is not part of a shared household) the
 * size is 1. A household of three or more members drives the {@code GOLD} membership tier (see
 * {@code OwnerMapper}).
 */
public class AssignHouseholdSize {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            owner.setHouseholdSize(1);
            return;
        }
        int count = 1;
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(existing.getHouseholdId())) {
                count++;
            }
        }
        owner.setHouseholdSize(count);
    }
}
