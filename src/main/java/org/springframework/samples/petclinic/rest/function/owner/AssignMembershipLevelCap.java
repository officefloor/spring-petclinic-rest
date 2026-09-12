package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.MembershipLevel;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code membershipLevelCap}: the ceiling applied to a new owner's
 * derived {@link MembershipLevel} so it cannot exceed one above the current maximum
 * membership level among their existing household members. The cap is
 * {@code maxExistingHouseholdMemberLevel + 1}; when the owner has no existing household
 * member the cap is left {@code null} and no ceiling applies.
 *
 * <p>Runs after {@link AssignHousehold} (so the shared {@code householdId} is set) and
 * {@link AssignHouseholdSize}, and before {@link SaveOwner}, so the maximum is taken over
 * the owners that existed before this create (the new owner is not yet persisted). A
 * soft-deleted owner is not a household member and is ignored. The value is fixed at
 * creation time and persisted with the owner; {@link MembershipLevel#of(Owner)} then
 * bounds the returned level by it.
 */
public class AssignMembershipLevelCap {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return; // not linked into a household: no ceiling
        }
        Integer maxLevel = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner is no longer a household member
            }
            if (householdId.equals(existing.getHouseholdId())) {
                int level = MembershipLevel.of(existing);
                if (maxLevel == null || level > maxLevel) {
                    maxLevel = level;
                }
            }
        }
        if (maxLevel != null) {
            owner.setMembershipLevelCap(maxLevel + 1);
        }
    }
}
