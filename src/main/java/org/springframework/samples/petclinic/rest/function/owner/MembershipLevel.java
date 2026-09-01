package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's numeric membership level from 1 to 3: starts at 1, adds 1 when an email is
 * present, adds 1 when {@code namesakeCount} is 0, and is capped at 3 (level 4 is reserved for
 * tenure).
 */
public final class MembershipLevel {

    private MembershipLevel() {
    }

    public static int of(Owner owner, int namesakeCount) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            level++;
        }
        if (namesakeCount == 0) {
            level++;
        }
        return Math.min(level, 3);
    }
}
