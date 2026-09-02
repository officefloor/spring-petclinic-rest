package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Caps an owner's {@link MembershipLevel} at one above the highest level among the OTHER members of
 * its {@link HouseholdId household}. Levels are at least 1, so a maximum of 0 means the owner has no
 * existing household member and the level is returned unchanged (no cap applies).
 */
public final class HouseholdLevelCeiling {

    private HouseholdLevelCeiling() {
    }

    public static int cap(Owner owner, int membershipLevel, OwnerRepository repository) {
        String householdId = HouseholdId.of(owner);
        int max = 0;
        for (Owner other : repository.findAll()) {
            if (!Objects.equals(other.getId(), owner.getId()) && householdId.equals(HouseholdId.of(other))) {
                max = Math.max(max, MembershipLevel.of(MembershipPoints.of(other,
                        NamesakeCount.of(other, repository), HouseholdSize.of(other, repository))));
            }
        }
        return max == 0 ? membershipLevel : Math.min(membershipLevel, max + 1);
    }
}
