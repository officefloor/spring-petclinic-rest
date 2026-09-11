package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code householdSize}: the number of owners sharing this owner's
 * {@code householdId} after this create, i.e. every existing member already carrying the
 * same id (back-filled by {@link AssignHousehold}) plus this owner. Runs after
 * {@link AssignHousehold} but before the owner is saved, so the new owner is counted with
 * a {@code +1}. Owners not in a shared household (null {@code householdId}) count as one.
 */
public class AssignHouseholdSize {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            owner.setHouseholdSize(1);
            return;
        }
        int count = 1; // this owner, not yet saved
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(existing.getHouseholdId())) {
                count++;
            }
        }
        owner.setHouseholdSize(count);
    }
}
