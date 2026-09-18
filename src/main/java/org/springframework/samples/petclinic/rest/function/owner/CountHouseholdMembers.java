package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records on the newly built owner how many members its household will have once this create
 * completes — the existing owners sharing its {@code householdId} plus this one. Runs after
 * {@link AssignHouseholdId} has stamped the deterministic id and before the owner is saved, so the
 * size reflects the household immediately after this create. An owner belonging to no household (no
 * postcode) has a size of 1.
 */
public class CountHouseholdMembers {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int existing = Household.existingMembers(owner, ownerRepository).size();
        owner.setHouseholdSize(existing + 1);
    }
}
