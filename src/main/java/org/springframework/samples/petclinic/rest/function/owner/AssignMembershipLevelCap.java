package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.MembershipLevels;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the new owner's {@code membershipLevelCap}: a ceiling of one above the current
 * maximum membership level among their existing household members (owners already sharing
 * this owner's {@code householdId}). Runs after {@link AssignHousehold} sets the id but
 * before the owner is saved, so only pre-existing members are considered. When the owner
 * has no household ({@code null} id) or no existing household member, no cap applies and
 * the field is left {@code null}. {@link MembershipLevels#forOwner(Owner)} enforces the cap.
 */
public class AssignMembershipLevelCap {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return;
        }
        int maxLevel = -1;
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(existing.getHouseholdId())) {
                maxLevel = Math.max(maxLevel, MembershipLevels.forOwner(existing));
            }
        }
        if (maxLevel >= 0) {
            owner.setMembershipLevelCap(maxLevel + 1);
        }
    }
}
