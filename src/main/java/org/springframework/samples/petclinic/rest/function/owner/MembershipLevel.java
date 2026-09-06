package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's numeric membership level (1 to 3) on creation: it starts at
 * 1, gains 1 when an email is present, gains 1 when {@code namesakeCount} is 0,
 * and is capped at 3 (level 4 is reserved for tenure).
 */
public final class MembershipLevel {

    private MembershipLevel() {
    }

    /** Membership level (1 to 3) derived from the owner's email and namesake count. */
    public static int of(Owner owner) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            level += 1;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level += 1;
        }
        return Math.min(level, 3);
    }
}
