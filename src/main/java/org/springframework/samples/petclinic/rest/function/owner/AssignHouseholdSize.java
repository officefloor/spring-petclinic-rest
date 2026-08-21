package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the {@code householdSize} to a newly built owner: the number of owners that share the
 * same {@code householdId} once this owner is included. Runs after {@link AssignHousehold} (which
 * stamps and back-fills the household id) and before the owner is saved, so the count reflects the
 * household as it stands after this create. When the owner joined no household ({@code householdId}
 * is null) the household is just this owner, so the size is 1.
 */
public class AssignHouseholdSize {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            owner.setHouseholdSize(1);
            return;
        }
        int count = 1; // the owner being created is not yet persisted
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(existing.getHouseholdId())) {
                count++;
            }
        }
        owner.setHouseholdSize(count);
    }
}
