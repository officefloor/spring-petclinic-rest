package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.MembershipLevel;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners}: records the membership-level ceiling for the new owner.
 *
 * <p>A new owner joining an existing household cannot be granted a membership level more than one
 * above the highest level already held by a household member. This step scans the owners already
 * sharing the new owner's {@code householdId}, takes the maximum of their derived membership levels
 * and stores {@code max + 1} as the new owner's {@code membershipLevelCap}. {@link MembershipLevel}
 * then clamps the points-derived level to this cap when producing the response and audit line.
 *
 * <p>With no existing household member (or no household at all) there is no ceiling, so the cap is
 * left null and the points-derived level stands. Runs after {@link AssignHouseholdId} has stamped
 * the {@code householdId} and before the owner is persisted, so the new owner itself is not yet
 * among the existing members counted here.
 */
public class CapMembershipLevel {

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
