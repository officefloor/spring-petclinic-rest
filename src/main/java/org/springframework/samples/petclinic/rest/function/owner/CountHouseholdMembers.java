package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records on the newly built owner how many members its household will have once this create
 * completes — the existing owners sharing its {@code householdId} plus this one. Runs after
 * {@link AssignHousehold} has assigned the shared id and before the owner is saved, so the size
 * reflects the household immediately after this create. An owner not sharing a household has a
 * size of 1.
 */
public class CountHouseholdMembers {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null || householdId.isBlank()) {
            owner.setHouseholdSize(1);
            return;
        }
        long existing = ownerRepository.findByLastName(owner.getLastName()).stream()
            .filter(member -> householdId.equals(member.getHouseholdId()))
            .count();
        owner.setHouseholdSize((int) (existing + 1));
    }
}
