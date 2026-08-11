package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners}: records on the owner the size of its household after this
 * create — the number of owners sharing its {@code householdId} once the new owner is included.
 * Runs after {@link AssignHouseholdId} (which stamps the shared identifier on the new owner and
 * its existing housemates) and before the owner is persisted, so it counts the existing members
 * carrying the same {@code householdId} plus the new owner itself.
 *
 * <p>An owner with no household leaves the size null.
 */
public class CountHouseholdMembers {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return;
        }
        int count = 1; // the new owner, not yet persisted
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(existing.getHouseholdId())) {
                count++;
            }
        }
        owner.setHouseholdSize(count);
    }
}
