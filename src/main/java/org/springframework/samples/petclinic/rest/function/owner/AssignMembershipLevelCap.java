package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.MembershipLevel;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the new owner's {@code membershipLevelCap}: a ceiling on its derived
 * {@code membershipLevel} equal to one above the current maximum membership level among its existing
 * household members (those already persisted sharing the new owner's {@code householdId}). The
 * derived level is then capped at read time by {@link MembershipLevel#of(Owner)}.
 *
 * <p>Runs after {@link AssignHousehold} (the {@code householdId} is set) and before
 * {@link SaveOwner}, so the scan sees the existing members but not the new owner itself. When the
 * owner has no {@code householdId} or no existing household member, no cap is set (the field stays
 * {@code null}) and the derived level applies unchanged.
 */
public class AssignMembershipLevelCap {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return;
        }
        Integer maxLevel = null;
        for (Owner existing : ownerRepository.findAll()) {
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
