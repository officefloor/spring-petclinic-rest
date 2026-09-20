package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records how many owners share the new owner's household. Counts the existing owners with
 * the same {@code householdId} (assigned earlier by {@link AssignHousehold}) and adds one
 * for the owner being created, so the count reflects the household size that results from
 * this create. An owner not sharing a household ({@code householdId} is {@code null}) is a
 * household of one.
 */
public class CountHouseholdMembers {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int count = 1;
        String householdId = owner.getHouseholdId();
        if (householdId != null) {
            for (Owner existing : ownerRepository.findAll()) {
                if (householdId.equals(existing.getHouseholdId())) {
                    count++;
                }
            }
        }
        owner.setHouseholdMemberCount(count);
    }
}
