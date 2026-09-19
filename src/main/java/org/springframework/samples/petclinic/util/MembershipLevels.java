package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's numeric membership level. The level starts at {@link #BASE} and
 * gains a point for a present email and a point for having no namesakes
 * ({@code namesakeCount} is 0), capped at {@link #MAX} (level 4 is reserved for tenure).
 */
public final class MembershipLevels {

    /** Level every owner starts at. */
    public static final int BASE = 1;

    /** Highest level derivable here; level 4 is reserved for tenure. */
    public static final int MAX = 3;

    private MembershipLevels() {
    }

    /** Return the membership level (1 to 3) for {@code owner}. */
    public static int of(Owner owner) {
        int level = BASE;
        if (owner.getEmail() != null && !owner.getEmail().isEmpty()) {
            level++;
        }
        if (Integer.valueOf(0).equals(owner.getNamesakeCount())) {
            level++;
        }
        return Math.min(level, MAX);
    }
}
