package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code householdSize}: the number of owners sharing this owner's
 * {@code householdId} once this create completes.
 *
 * <p>Runs in the {@code POST /api/owners} pipeline after {@link AssignOwnerHousehold} (so the shared
 * {@code householdId} is already set and existing members are backfilled) and before
 * {@link SaveOwner} (so the new owner is not yet persisted). The count is therefore every existing
 * owner carrying the same id plus one for the owner being created. An owner that did not opt into a
 * household has no {@code householdId} and counts as a household of one. The value is frozen at
 * creation time and drives the 'GOLD' membership tier (3 or more members).
 */
public class CountOwnerHousehold {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        int count = 1; // the owner being created, not yet persisted
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
