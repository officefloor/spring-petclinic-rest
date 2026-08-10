package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Runs after {@link AssignOwnerHousehold} (which sets and back-fills the {@code householdId}) and before
 * the owner is saved. Sets the owner's {@code householdSize} to the number of members its household has
 * after this create: the existing owners already sharing the same {@code householdId} plus this new one
 * (not yet persisted). Owners with no household ({@code householdId == null}) are a household of one. The
 * value is mutated in place via {@code @Val} so the save/respond steps persist and return it.
 */
public class CountHouseholdMembers {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        int size = 1; // this owner, not yet persisted
        if (householdId != null) {
            size += (int) ownerRepository.findAll().stream()
                    .filter(existing -> householdId.equals(existing.getHouseholdId()))
                    .count();
        }
        owner.setHouseholdSize(size);
    }
}
