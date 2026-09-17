package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.util.MembershipLevel;
import org.springframework.samples.petclinic.util.MembershipPoints;

/**
 * Captures the ceiling on the new owner's membership level: one above the highest
 * {@link MembershipLevel membership level} among the owners already sharing its
 * {@link HouseholdNormalizer#id(String, String) household id} (same last name and postcode). Runs
 * before {@link SaveOwner}, so the count is taken over existing household members only; with no
 * existing member no cap applies and the ceiling stays {@code null}. The stored cap is applied when
 * the owner's level is read (see {@code OwnerMapper#membershipLevel}). Mutates the built
 * {@link Owner} in place.
 */
public class CapMembershipLevel {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        Integer maxLevel = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (HouseholdNormalizer.belongsTo(existing, householdId)) {
                Integer level = MembershipLevel.forPoints(MembershipPoints.of(existing));
                if (level != null && (maxLevel == null || level > maxLevel)) {
                    maxLevel = level;
                }
            }
        }
        owner.setMembershipLevelCap(maxLevel == null ? null : maxLevel + 1);
    }
}
