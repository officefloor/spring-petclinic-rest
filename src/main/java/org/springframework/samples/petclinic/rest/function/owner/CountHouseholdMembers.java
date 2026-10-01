package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Runs after {@link AssignHousehold} (so the {@code householdId} is set) and before
 * {@link SaveOwner} on {@code POST /api/owners}. Records the new owner's
 * {@code householdMemberCount}: the number of members this owner's household has after this
 * create, i.e. the existing owners that share the same {@code householdId} plus the new owner
 * itself. The owner is still unsaved, so it is counted by the trailing {@code + 1} rather than by
 * iterating the repository.
 *
 * <p>Records the household size for reporting; it no longer affects the membership level, which is
 * derived from email presence and {@code namesakeCount} (see {@code MembershipLevel}).
 */
public class CountHouseholdMembers {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId != null && householdId.equals(existing.getHouseholdId())) {
                count++;
            }
        }
        owner.setHouseholdMemberCount(count + 1); // + 1 for this still-unsaved owner
    }
}
