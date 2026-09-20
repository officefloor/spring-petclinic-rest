package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.MembershipLevel;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Caps a new owner's membership level relative to their household. A new owner's level
 * cannot exceed one above the highest membership level currently held by an existing member
 * of their household (owners sharing the same last name and postcode, see
 * {@link Households#sameHousehold}). The ceiling is stored on the owner and applied wherever
 * the level is derived (see {@link MembershipLevel#of}); {@link MembershipLevel} maps points
 * to a level.
 *
 * <p>The owner being created is not yet saved, so {@link OwnerRepository#findAllActive()}
 * yields only existing members. When there is no existing household member no ceiling is set
 * and no cap applies.
 */
public class CapMembershipLevel {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        Integer maxLevel = null;
        for (Owner existing : ownerRepository.findAllActive()) {
            if (Households.sameHousehold(owner, existing)) {
                int level = MembershipLevel.of(existing);
                maxLevel = (maxLevel == null) ? level : Math.max(maxLevel, level);
            }
        }
        if (maxLevel != null) {
            owner.setMembershipLevelCap(maxLevel + 1);
        }
    }
}
