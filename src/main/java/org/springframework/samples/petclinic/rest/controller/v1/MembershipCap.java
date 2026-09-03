package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Collection;
import java.util.OptionalInt;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Caps a new owner's membership level relative to their household. A new owner's level cannot
 * exceed one above the current maximum {@link MembershipLevel} among their existing household
 * members; with no existing household member no cap applies.
 */
public final class MembershipCap {

    private MembershipCap() {
    }

    /**
     * The level ceiling for {@code candidate}: one above the highest {@link MembershipLevel} among
     * {@code existing} owners sharing its household, or {@code null} when it has no household peer.
     */
    public static Integer of(Owner candidate, Collection<Owner> existing) {
        OptionalInt max = existing.stream()
            .filter(o -> !o.isDeleted())
            .filter(o -> candidate.getHouseholdId() != null
                && candidate.getHouseholdId().equals(o.getHouseholdId()))
            .mapToInt(MembershipLevel::of)
            .max();
        return max.isPresent() ? max.getAsInt() + 1 : null;
    }

    /** The owner's {@link MembershipLevel} limited to its stored {@link Owner#getMembershipCap()}. */
    public static int level(Owner owner) {
        int level = MembershipLevel.of(owner);
        Integer cap = owner.getMembershipCap();
        return cap == null ? level : Math.min(level, cap);
    }
}
