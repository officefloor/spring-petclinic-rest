package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's numeric membership level, assigned on creation. The level starts at
 * 1, gains 1 when the owner has an email address, gains 1 when the owner has no namesakes
 * ({@code namesakeCount == 0}), and is capped at {@link #MAX} (level 4 is reserved for
 * tenure).
 */
public final class MembershipLevel {

    /** Level assigned to every owner before any bonuses. */
    public static final int BASE = 1;

    /** Highest level assignable on creation; level 4 is reserved for tenure. */
    public static final int MAX = 3;

    private MembershipLevel() {
    }

    /** The membership level for the given owner, from {@link #BASE} to {@link #MAX}. */
    public static int of(Owner owner) {
        int level = BASE;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        return Math.min(level, MAX);
    }
}
