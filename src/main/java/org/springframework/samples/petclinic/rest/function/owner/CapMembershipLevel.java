package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners} that records the owner's membership-level ceiling: a new owner's
 * {@code membershipLevel} cannot exceed one above the current maximum {@code membershipLevel} among
 * its household members ({@link Household#id(String, String) same householdId}, i.e. same last name
 * and postcode). Runs after {@link CountHouseholdMembers} (so the household state is set) and before
 * {@link SaveOwner} (so the owner being created is not counted as its own household member). The
 * ceiling is {@code maxHouseholdMemberLevel + 1}, stored on the owner as {@code membershipLevelCap}
 * and applied by {@link MembershipLevel#of(Owner)} when the level is exposed. With no existing
 * household member no ceiling is recorded and no cap applies.
 */
public class CapMembershipLevel {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = Household.id(owner.getLastName(), owner.getPostcode());
        Integer maxLevel = null;
        for (Owner other : ownerRepository.findAll()) {
            if (householdId.equals(Household.id(other.getLastName(), other.getPostcode()))) {
                int level = MembershipLevel.of(other);
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
