package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records the size of the owner's household: the number of owners that share this owner's
 * {@code householdId}, counting the owner itself. An owner with no {@code householdId} is a
 * household of one. Household membership is assigned by {@link AssignHousehold}. Runs on both the
 * create and read pipelines so the count is derived consistently from current data.
 */
public class AssignHouseholdMemberCount {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            owner.setHouseholdMemberCount(1);
            return;
        }
        long count = ownerRepository.findAll().stream()
                .filter(existing -> householdId.equals(existing.getHouseholdId()))
                .count();
        owner.setHouseholdMemberCount((int) count);
    }
}
