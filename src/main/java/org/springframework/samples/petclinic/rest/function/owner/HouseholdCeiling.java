package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Collection;
import java.util.Objects;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Level ceiling for an owner: their {@link MembershipLevel} may not exceed one above the current
 * maximum level among the OTHER members of their household (same {@code householdId}). With no
 * other household member the ceiling does not apply. Mirrors the household-factor weighting used
 * by {@link Membership} so a member's level is measured on the same scale.
 */
public final class HouseholdCeiling {

    private static final int HOUSEHOLD_POINTS = 2;

    private static final int HOUSEHOLD_THRESHOLD = 3;

    private HouseholdCeiling() {
    }

    public static int cap(Owner owner, int level, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        Collection<Owner> all = ownerRepository.findAll();
        long size = all.stream().filter(other -> Objects.equals(householdId, other.getHouseholdId())).count();
        int householdPoints = size >= HOUSEHOLD_THRESHOLD ? HOUSEHOLD_POINTS : 0;
        int max = -1;
        for (Owner other : all) {
            if (!Objects.equals(other.getId(), owner.getId())
                    && Objects.equals(householdId, other.getHouseholdId())) {
                max = Math.max(max, MembershipLevel.of(MembershipPoints.of(other) + householdPoints));
            }
        }
        return max < 0 ? level : Math.min(level, max + 1);
    }
}
