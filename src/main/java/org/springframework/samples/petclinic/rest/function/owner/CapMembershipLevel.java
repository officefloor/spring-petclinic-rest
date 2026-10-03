package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Caps the new owner's {@code membershipLevel} at one above the current maximum
 * {@code membershipLevel} among their household members — the existing owners that share
 * the owner's household ({@code (lastName, postcode)}, keyed by {@link Households}).
 * Runs after {@link AssignHousehold} has stamped the {@code householdId}, so it reflects
 * that same household definition, and before the owner is saved, so the owner being
 * created is not counted among the existing members.
 *
 * <p>The cap is the highest member level plus one, stored on the owner so that
 * {@link Owner#getMembershipLevel()} clamps the banded value to it. With no existing
 * household member (including an owner with no postcode, which belongs to no household)
 * no cap is set and the banded level stands. Which owners count as household members is
 * defined once by {@link Households#members}.
 */
public class CapMembershipLevel {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        Integer maxLevel = null;
        for (Owner member : Households.members(owner.getHouseholdId(), ownerRepository.findAll())) {
            Integer level = member.getMembershipLevel();
            if (level != null && (maxLevel == null || level > maxLevel)) {
                maxLevel = level;
            }
        }
        if (maxLevel != null) {
            owner.setMembershipLevelCap(maxLevel + 1);
        }
    }
}
