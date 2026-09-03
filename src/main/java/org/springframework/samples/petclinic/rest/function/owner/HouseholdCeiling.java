package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Caps an owner's membershipLevel at one above the current maximum membershipLevel among
 * their existing household members (other owners sharing the same householdId). With no
 * existing household member no cap applies.
 */
public final class HouseholdCeiling {

    private HouseholdCeiling() {
    }

    /** The level, capped at one above the highest level among existing household members. */
    public static int cap(int level, Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return level;
        }
        int ceiling = -1;
        for (Owner other : ownerRepository.findAll()) {
            if (isMember(owner, other, householdId)) {
                ceiling = Math.max(ceiling, MembershipLevel.level(MembershipLevel.points(other, ownerRepository)));
            }
        }
        return ceiling < 0 ? level : Math.min(level, ceiling + 1);
    }

    /** True when {@code other} is a distinct owner in the same household as {@code owner}. */
    private static boolean isMember(Owner owner, Owner other, String householdId) {
        if (other == owner || !householdId.equals(other.getHouseholdId())) {
            return false;
        }
        return owner.getId() == null || !owner.getId().equals(other.getId());
    }
}
