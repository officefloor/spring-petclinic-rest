package org.springframework.samples.petclinic.util;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Caps a new owner's membership level at one above the highest level currently held
 * by any other member of their household (owners sharing the same {@link Household}
 * id). With no existing household member, no ceiling applies.
 */
public final class LevelCeiling {

    private LevelCeiling() {
    }

    /**
     * {@code level} reduced to at most one above the maximum membership level among
     * {@code owner}'s existing household members in {@code allOwners} (excluding the
     * owner itself and soft-deleted records). Returns {@code level} unchanged when
     * the household has no other member.
     */
    public static int cap(int level, Owner owner, Collection<Owner> allOwners) {
        String household = Household.idFor(owner);
        int max = -1;
        for (Owner other : allOwners) {
            boolean self = other.getId() != null && other.getId().equals(owner.getId());
            if (self || other.isDeleted() || !household.equals(Household.idFor(other))) {
                continue;
            }
            max = Math.max(max, Membership.level(Membership.points(other)));
        }
        return max < 0 ? level : Math.min(level, max + 1);
    }
}
