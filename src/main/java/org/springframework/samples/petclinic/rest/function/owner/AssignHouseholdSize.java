package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code householdSize}: the number of owners that share the new
 * owner's {@code householdId} once this create is included. Runs after
 * {@link AssignHousehold} (so the shared {@code householdId} is set and back-filled onto
 * the existing housemates) and before {@link SaveOwner}, so the value counts the existing
 * housemates plus the one being created. An owner not linked into a household (null
 * {@code householdId}) is a household of one. The value is fixed at creation time and
 * persisted with the owner.
 */
public class AssignHouseholdSize {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        int size = 1; // the owner being created (not yet persisted)
        if (householdId != null) {
            for (Owner existing : ownerRepository.findAll()) {
                if (householdId.equals(existing.getHouseholdId())) {
                    size++;
                }
            }
        }
        owner.setHouseholdSize(size);
    }
}
