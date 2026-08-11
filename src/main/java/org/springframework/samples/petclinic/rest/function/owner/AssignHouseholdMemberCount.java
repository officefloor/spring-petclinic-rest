package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code householdMemberCount}: the number of owners in this owner's household
 * (all owners sharing the same computed {@code householdId}) once this create completes, including
 * the new owner itself. Runs after {@link AssignHouseholdId} has assigned the deterministic
 * {@code householdId}, but before the new owner is saved, so the existing members are counted from
 * the repository and the not-yet-persisted new owner is added in as one.
 *
 * <p>Because the householdId is keyed on (lastName, postcode), the members counted here are exactly
 * the owners sharing this owner's last name and postcode.
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
