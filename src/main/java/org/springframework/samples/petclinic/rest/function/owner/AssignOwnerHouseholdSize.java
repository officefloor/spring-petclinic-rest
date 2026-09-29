package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code householdSize} — the number of owners sharing the same
 * {@code householdId} after this create, counting the new owner. Runs after
 * {@link AssignHousehold} has assigned the shared {@code householdId} (and backfilled the
 * existing members) but before the new owner is saved, so it counts the persisted members
 * plus one for the new owner. Owners without a household have a size of one (themselves).
 * A household of three or more members makes the owner {@code GOLD}.
 */
public class AssignOwnerHouseholdSize {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        long existing = householdId == null ? 0 : ownerRepository.findAll().stream()
                .filter(other -> householdId.equals(other.getHouseholdId()))
                .count();
        owner.setHouseholdSize((int) (existing + 1));
    }
}
