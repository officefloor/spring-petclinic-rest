package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.MembershipLevel;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Runs after {@link AssignHousehold} (so the {@code householdId} is set) and before
 * {@link SaveOwner} on {@code POST /api/owners}. Caps the new owner's membership level so it cannot
 * exceed one above the current maximum {@link MembershipLevel level} among the existing members of
 * the owner's household (the already-saved owners sharing the same {@code householdId}).
 *
 * <p>The computed ceiling is stored on the owner as its
 * {@link Owner#getMembershipLevelCap() membership level cap}; {@link MembershipLevel#of(Owner)} then
 * applies it whenever the level is derived, so the capped value is what every response returns. When
 * the household has no existing member — no match, or the owner has no {@code householdId} (no
 * postcode) — no cap applies and the owner keeps its uncapped level.
 */
public class CapMembershipLevel {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return; // no household to key on, so no cap
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
