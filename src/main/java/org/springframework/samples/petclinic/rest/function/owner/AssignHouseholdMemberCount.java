package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code householdMemberCount}: the number of owners in this owner's household
 * (all owners sharing the same {@code householdId}) once this create completes, including the new
 * owner itself. Runs after {@link AssignHouseholdId} has assigned and back-filled the shared
 * {@code householdId}, but before the new owner is saved, so the existing members are counted from
 * the repository and the not-yet-persisted new owner is added in as one.
 *
 * <p>An owner not joining a household has no {@code householdId}; its household is just itself, so
 * the count is 1.
 */
public class AssignHouseholdMemberCount {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        int count = 1; // the new owner, not yet saved
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
