package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners} that records how many owners belong to this owner's household
 * once this create completes. {@code householdSize} is the number of existing owners that already
 * share this owner's {@code householdId} plus one for the owner being created. Runs after
 * {@link AssignHouseholdId} (so the householdId is set) and before {@link SaveOwner} (so the owner
 * being created is not yet in the store and is counted exactly once via the {@code + 1}). Owners
 * with no householdId are a household of one. This value drives the {@code GOLD} membership tier
 * (3+ members).
 */
public class AssignHouseholdSize {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        int size = 1;
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
